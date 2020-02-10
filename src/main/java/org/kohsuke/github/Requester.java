/*
 * The MIT License
 *
 * Copyright (c) 2010, Kohsuke Kawaguchi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.kohsuke.github;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

/**
 * A builder pattern for making HTTP call and parsing its output.
 *
 * @author Kohsuke Kawaguchi
 */
class Requester extends GitHubRequest.Builder<Requester> {
    private final GitHubClient client;

    Requester(GitHubClient client) {
        this.client = client;
    }

    /**
     * Sends a request to the specified URL and checks that it is sucessful.
     *
     * @throws IOException
     *             the io exception
     */
    public void send() throws IOException {
        // Send expects there to be some body response, but doesn't care what it is.
        // If there isn't a body, this will throw.
        client.sendRequest(build(client), (responseInfo) -> responseInfo.getBodyAsString());
    }

    /**
     * Sends a request and parses the response into the given type via databinding.
     *
     * @param <T>
     *            the type parameter
     * @param type
     *            the type
     * @return {@link Reader} that reads the response.
     * @throws IOException
     *             if the server returns 4xx/5xx responses.
     */
    public <T> T fetch(@Nonnull Class<T> type) throws IOException {
        return client.sendRequest(build(client), (responseInfo) -> GitHubClient.parseBody(responseInfo, type)).body();
    }

    /**
     * Sends a request and parses the response into an array of the given type via databinding.
     *
     * @param <T>
     *            the type parameter
     * @param type
     *            the type
     * @return {@link Reader} that reads the response.
     * @throws IOException
     *             if the server returns 4xx/5xx responses.
     */
    public <T> T[] fetchArray(@Nonnull Class<T[]> type) throws IOException {
        return fetchArrayResponse(type).body();
    }

    <T> GitHubResponse<T[]> fetchArrayResponse(@Nonnull Class<T[]> type) throws IOException {
        GitHubResponse<T[]> result;

        try {
            // for arrays we might have to loop for pagination
            // use the iterator to handle it
            List<T[]> pages = new ArrayList<>();
            int totalSize = 0;
            GitHubPageIterator<T[]> iterator = fetchIterator(type, 0);

            do {
                T[] item = iterator.next();
                totalSize += Array.getLength(item);
                pages.add(item);
            } while (iterator.hasNext());

            GitHubResponse<T[]> lastResponse = iterator.lastResponse();

            result = new GitHubResponse<>(lastResponse, concatenatePages(type, pages, totalSize));
        } catch (GHException e) {
            // if there was an exception inside the iterator it is wrapped as a GHException
            // if the wrapped exception is an IOException, throw that
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw e;
            }
        }

        return result;
    }

    /**
     * Like {@link #fetch(Class)} but updates an existing object instead of creating a new instance.
     *
     * @param <T>
     *            the type parameter
     * @param existingInstance
     *            the existing instance
     * @return the t
     * @throws IOException
     *             the io exception
     */
    public <T> T fetchInto(@Nonnull T existingInstance) throws IOException {
        return client
                .sendRequest(build(client), (responseInfo) -> GitHubClient.parseBody(responseInfo, existingInstance))
                .body();
    }

    /**
     * Makes a request and just obtains the HTTP status code. Method does not throw exceptions for many status codes
     * that would otherwise throw.
     *
     * @return the int
     * @throws IOException
     *             the io exception
     */
    public int fetchHttpStatusCode() throws IOException {
        return client.sendRequest(build(client), null).statusCode();
    }

    /**
     * Response input stream. There are scenarios where direct stream reading is needed, however it is better to use
     * {@link #fetch(Class)} where possible.
     *
     * @return the input stream
     * @throws IOException
     *             the io exception
     */
    public InputStream fetchStream() throws IOException {
        return client.sendRequest(build(client), (responseInfo) -> responseInfo.wrapInputStream()).body();
    }

    public <T> PagedIterable<T> fetchIterable(Class<T[]> type, Consumer<T> consumer) {
        return new PagedIterableWithConsumer<>(this, type, consumer);
    }

    static class PagedIterableWithConsumer<T> extends PagedIterable<T> {

        private final Class<T[]> clazz;
        private final Consumer<T> consumer;
        private Requester requester;

        PagedIterableWithConsumer(Requester requester, Class<T[]> clazz, Consumer<T> consumer) {
            this.clazz = clazz;
            this.consumer = consumer;
            this.requester = requester;
        }

        @Override
        @Nonnull
        public PagedIterator<T> _iterator(int pageSize) {
            final Iterator<T[]> iterator = requester.fetchIterator(clazz, pageSize);
            return new PagedIterator<T>(iterator) {
                @Override
                protected void wrapUp(T[] page) {
                    if (consumer != null) {
                        for (T item : page) {
                            consumer.accept(item);
                        }
                    }
                }
            };
        }
    }

    private <T> T[] concatenatePages(Class<T[]> type, List<T[]> pages, int totalLength) {

        T[] result = type.cast(Array.newInstance(type.getComponentType(), totalLength));

        int position = 0;
        for (T[] page : pages) {
            final int pageLength = Array.getLength(page);
            System.arraycopy(page, 0, result, position, pageLength);
            position += pageLength;
        }
        return result;
    }

    /**
     * Loads paginated resources.
     *
     * @param type
     *            type of each page (not the items in the page).
     * @param pageSize
     *            the size of the
     * @param <T>
     *            type of each page (not the items in the page).
     * @return
     */
    <T> GitHubPageIterator<T> fetchIterator(Class<T> type, int pageSize) {
        if (pageSize > 0)
            this.with("per_page", pageSize);

        try {
            GitHubRequest request = build(client);
            if (!"GET".equals(request.method())) {
                throw new IllegalStateException("Request method \"GET\" is required for iterator.");
            }
            return new GitHubPageIterator<>(client, type, request);
        } catch (IOException e) {
            throw new GHException("Unable to build github Api URL", e);
        }
    }
}
