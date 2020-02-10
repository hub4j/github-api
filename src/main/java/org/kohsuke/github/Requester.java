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

import com.fasterxml.jackson.databind.JsonMappingException;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import static java.util.logging.Level.*;
import static org.kohsuke.github.GitHubClient.MAPPER;

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
        parseResponse(null, null);
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
        return parseResponse(type, null).body();
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
        return fetchResponseArray(type).body();
    }

    <T> GitHubResponse<T[]> fetchResponseArray(@Nonnull Class<T[]> type) throws IOException {
        GitHubResponse<T[]> result;

        try {
            // for arrays we might have to loop for pagination
            // use the iterator to handle it
            List<T[]> pages = new ArrayList<>();
            GitHubResponse<T[]> lastResponse;
            int totalSize = 0;
            PagingResponseIterator<T[]> iterator = asResponseIterator(type, 0);

            do {
                lastResponse = iterator.next();
                totalSize += Array.getLength(lastResponse.body());
                pages.add(lastResponse.body());
            } while (iterator.hasNext());

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
        return parseResponse(null, existingInstance).body();
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
        return parseResponse(InputStream.class, null).body();
    }

    @Nonnull
    private <T> GitHubResponse<T> parseResponse(Class<T> type, T instance) throws IOException {
        return parseResponse(build(client), type, instance);
    }

    @Nonnull
    private <T> GitHubResponse<T> parseResponse(GitHubRequest request, Class<T> type, T instance) throws IOException {
        return client.sendRequest(request, (responseInfo) -> parse(responseInfo, type, instance));
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

    <T> PagedIterable<T> toIterable(Class<T[]> type, Consumer<T> consumer) {
        return new PagedIterableWithConsumer<>(type, consumer);
    }

    class PagedIterableWithConsumer<T> extends PagedIterable<T> {

        private final Class<T[]> clazz;
        private final Consumer<T> consumer;

        PagedIterableWithConsumer(Class<T[]> clazz, Consumer<T> consumer) {
            this.clazz = clazz;
            this.consumer = consumer;
        }

        @Override
        @Nonnull
        public PagedIterator<T> _iterator(int pageSize) {
            final Iterator<T[]> iterator = asIterator(clazz, pageSize);
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

    /**
     * May be used for any item that has pagination information.
     *
     * Works for array responses, also works for search results which are single instances with an array of items
     * inside.
     *
     * @param <T>
     *            type of each page (not the items in the page).
     */
    static class PagingResponseIterator<T> implements Iterator<GitHubResponse<T>> {

        private final GitHubClient client;
        private final Class<T> type;
        private final Requester requester;
        private GitHubRequest nextRequest;
        private GitHubResponse<T> next;

        PagingResponseIterator(Requester requester, GitHubClient client, Class<T> type, GitHubRequest request) {
            this.client = client;
            this.type = type;
            this.nextRequest = request;
            this.requester = requester;
        }

        public boolean hasNext() {
            fetch();
            return next != null;
        }

        public GitHubResponse<T> next() {
            fetch();
            GitHubResponse<T> r = next;
            if (r == null)
                throw new NoSuchElementException();
            next = null;
            return r;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        private void fetch() {
            if (next != null)
                return; // already fetched
            if (nextRequest == null)
                return; // no more data to fetch

            URL url = nextRequest.url();
            try {
                next = requester.parseResponse(nextRequest, type, null);
                assert next.body() != null;
                nextRequest = findNextURL();
            } catch (IOException e) {
                throw new GHException("Failed to retrieve " + url, e);
            }
        }

        /**
         * Locate the next page from the pagination "Link" tag.
         */
        private GitHubRequest findNextURL() throws MalformedURLException {
            GitHubRequest result = null;
            String link = next.headerField("Link");
            if (link != null) {
                for (String token : link.split(", ")) {
                    if (token.endsWith("rel=\"next\"")) {
                        // found the next page. This should look something like
                        // <https://api.github.com/repos?page=3&per_page=100>; rel="next"
                        int idx = token.indexOf('>');
                        result = next.request().builder().build(client, new URL(token.substring(1, idx)));
                        break;
                    }
                }
            }
            return result;
        }
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
    private <T> PagingResponseIterator<T> asResponseIterator(Class<T> type, int pageSize) {
        if (pageSize > 0)
            this.with("per_page", pageSize);

        try {
            GitHubRequest request = build(client);
            if (!"GET".equals(request.method())) {
                throw new IllegalStateException("Request method \"GET\" is required for iterator.");
            }
            return new PagingResponseIterator<>(this, client, type, request);
        } catch (IOException e) {
            throw new GHException("Unable to build github Api URL", e);
        }
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
    <T> Iterator<T> asIterator(Class<T> type, int pageSize) {
        PagingResponseIterator<T> delegate = asResponseIterator(type, pageSize);
        return new PagingIterator<>(delegate);
    }

    /**
     * May be used for any item that has pagination information.
     *
     * Works for array responses, also works for search results which are single instances with an array of items
     * inside.
     *
     * @param <T>
     *            type of each page (not the items in the page).
     */
    static class PagingIterator<T> implements Iterator<T> {

        private final PagingResponseIterator<T> delegate;

        PagingIterator(PagingResponseIterator<T> delegate) {
            this.delegate = delegate;
        }

        public boolean hasNext() {
            return delegate.hasNext();
        }

        public T next() {
            GitHubResponse<T> response = delegate.next();
            assert response.body() != null;
            return response.body();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    @CheckForNull
    private <T> T parse(GitHubResponse.ResponseInfo responseInfo, Class<T> type, T instance) throws IOException {
        if (responseInfo.statusCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
            return null; // special case handling for 304 unmodified, as the content will be ""
        }
        if (responseInfo.statusCode() == HttpURLConnection.HTTP_NO_CONTENT && type != null && type.isArray()) {
            // no content
            return type.cast(Array.newInstance(type.getComponentType(), 0));
        }

        // Response code 202 means data is being generated.
        // This happens in specific cases:
        // statistics - See https://developer.github.com/v3/repos/statistics/#a-word-about-caching
        // fork creation - See https://developer.github.com/v3/repos/forks/#create-a-fork
        if (responseInfo.statusCode() == HttpURLConnection.HTTP_ACCEPTED) {
            if (responseInfo.url().toString().endsWith("/forks")) {
                LOGGER.log(INFO, "The fork is being created. Please try again in 5 seconds.");
            } else if (responseInfo.url().toString().endsWith("/statistics")) {
                LOGGER.log(INFO, "The statistics are being generated. Please try again in 5 seconds.");
            } else {
                LOGGER.log(INFO,
                        "Received 202 from " + responseInfo.url().toString() + " . Please try again in 5 seconds.");
            }
            // Maybe throw an exception instead?
            return null;
        }

        if (type != null && type.equals(InputStream.class)) {
            return type.cast(responseInfo.wrapInputStream());
        }

        InputStreamReader r = null;
        String data;
        try {
            r = new InputStreamReader(responseInfo.wrapInputStream(), StandardCharsets.UTF_8);
            data = IOUtils.toString(r);
        } finally {
            IOUtils.closeQuietly(r);
        }

        try {
            if (type != null) {
                return setResponseHeaders(responseInfo, MAPPER.readValue(data, type));
            } else if (instance != null) {
                return setResponseHeaders(responseInfo, MAPPER.readerForUpdating(instance).<T>readValue(data));
            }
        } catch (JsonMappingException e) {
            String message = "Failed to deserialize " + data;
            throw new IOException(message, e);
        }
        return null;

    }

    private <T> T setResponseHeaders(GitHubResponse.ResponseInfo responseInfo, T readValue) {
        if (readValue instanceof GHObject[]) {
            for (GHObject ghObject : (GHObject[]) readValue) {
                setResponseHeaders(responseInfo, ghObject);
            }
        } else if (readValue instanceof GHObject) {
            setResponseHeaders(responseInfo, (GHObject) readValue);
        } else if (readValue instanceof JsonRateLimit) {
            // if we're getting a GHRateLimit it needs the server date
            ((JsonRateLimit) readValue).resources.getCore().recalculateResetDate(responseInfo.headerField("Date"));
        }
        return readValue;
    }

    private void setResponseHeaders(GitHubResponse.ResponseInfo responseInfo, GHObject readValue) {
        readValue.responseHeaderFields = responseInfo.headers();
    }

    private static final Logger LOGGER = Logger.getLogger(Requester.class.getName());

}
