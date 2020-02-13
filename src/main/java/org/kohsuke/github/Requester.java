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
import java.util.Iterator;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

/**
 * A thin helper for {@link GitHubRequest.Builder} that includes {@link GitHubClient}.
 *
 * @author Kohsuke Kawaguchi
 */
class Requester extends GitHubRequest.Builder<Requester> {
    /* private */ final GitHubClient client;

    Requester(GitHubClient client) {
        this.client = client;
        this.withApiUrl(client.getApiUrl());
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
        client.sendRequest(this, (responseInfo) -> responseInfo.getBodyAsString());
    }

    /**
     * Sends a request and parses the response into the given type via databinding.
     *
     * @param <T>
     *            the type parameter
     * @param type
     *            the type
     * @return an instance of {@link T}
     * @throws IOException
     *             if the server returns 4xx/5xx responses.
     */
    public <T> T fetch(@Nonnull Class<T> type) throws IOException {
        return client.sendRequest(this, (responseInfo) -> GitHubResponse.parseBody(responseInfo, type)).body();
    }

    /**
     * Sends a request and parses the response into an array of the given type via databinding.
     *
     * @param <T>
     *            the type parameter
     * @param type
     *            the type
     * @return an array of {@link T} elements
     * @throws IOException
     *             if the server returns 4xx/5xx responses.
     */
    public <T> T[] fetchArray(@Nonnull Class<T[]> type) throws IOException {
        return toIterable(client, type, null).toArray();
    }

    /**
     * Like {@link #fetch(Class)} but updates an existing object instead of creating a new instance.
     *
     * @param <T>
     *            the type parameter
     * @param existingInstance
     *            the existing instance
     * @return the updated instance
     * @throws IOException
     *             the io exception
     */
    public <T> T fetchInto(@Nonnull T existingInstance) throws IOException {
        return client.sendRequest(this, (responseInfo) -> GitHubResponse.parseBody(responseInfo, existingInstance))
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
        return client.sendRequest(build(), null).statusCode();
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
        return client.sendRequest(this, (responseInfo) -> responseInfo.bodyStream()).body();
    }

    /**
     * Creates {@link PagedIterable <R>} from this builder using the provided {@link Consumer<R>}. This method and the
     * {@link PagedIterable <R>} do not actually begin fetching data until {@link Iterator#next()} or
     * {@link Iterator#hasNext()} are called.
     *
     * @param type
     *            the type of the pages to retrieve.
     * @param itemInitializer
     *            the consumer to execute on each paged item retrieved.
     * @param <R>
     *            the element type for the pages returned from
     * @return the {@link PagedIterable} for this builder.
     */
    public <R> PagedIterable<R> toIterable(Class<R[]> type, Consumer<R> itemInitializer) {
        return toIterable(this.client, type, itemInitializer);
    }
}
