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
import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.Reader;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.net.ssl.SSLHandshakeException;

import static java.util.logging.Level.*;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.kohsuke.github.GitHub.MAPPER;

/**
 * A builder pattern for making HTTP call and parsing its output.
 *
 * @author Kohsuke Kawaguchi
 */
class Requester extends GitHubRequest.Builder<Requester> {
    public static final int CONNECTION_ERROR_RETRIES = 2;
    private final GitHub root;

    /**
     * Current connection.
     */
    private GitHubResponse.ResponseInfo previousResponseInfo;

    /**
     * If timeout issues let's retry after milliseconds.
     */
    private static final int retryTimeoutMillis = 100;

    Requester(GitHub root) {
        this.root = root;
    }

    /**
     * Sends a request to the specified URL and checks that it is sucessful.
     *
     * @throws IOException
     *             the io exception
     */
    public void send() throws IOException {
        parseResponse(null, null).body();
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
        T[] result;

        try {
            // for arrays we might have to loop for pagination
            // use the iterator to handle it
            List<T[]> pages = new ArrayList<>();
            int totalSize = 0;
            for (Iterator<T[]> iterator = asIterator(type, 0); iterator.hasNext();) {
                T[] nextResult = iterator.next();
                totalSize += Array.getLength(nextResult);
                pages.add(nextResult);
            }

            result = concatenatePages(type, pages, totalSize);
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
        return sendRequest(build(root), null).statusCode();
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
        return sendRequest(build(root), (responseInfo) -> parse(responseInfo, type, instance));
    }

    @Nonnull
    private <T> GitHubResponse<T> sendRequest(GitHubRequest request, ResponsBodyHandler<T> parser) throws IOException {
        int retries = CONNECTION_ERROR_RETRIES;

        do {
            // if we fail to create a connection we do not retry and we do not wrap

            GitHubResponse.ResponseInfo responseInfo = null;
            try {
                responseInfo = GitHubResponse.ResponseInfo.fromHttpURLConnection(request, root);
                previousResponseInfo = responseInfo;
                noteRateLimit(responseInfo);
                detectOTPRequired(responseInfo);

                // for this workaround, we can retry now
                if (isInvalidCached404Response(responseInfo)) {
                    continue;
                }
                if (!(isRateLimitResponse(responseInfo) || isAbuseLimitResponse(responseInfo))) {
                    T body = null;
                    if (parser != null) {
                        body = parser.apply(responseInfo);
                    }
                    return new GitHubResponse<>(responseInfo, body);
                }
            } catch (IOException e) {
                // For transient errors, retry
                if (retryConnectionError(e, request.url(), retries)) {
                    continue;
                }

                throw interpretApiError(e, request, responseInfo);
            }

            handleLimitingErrors(responseInfo);

        } while (--retries >= 0);

        throw new GHIOException("Ran out of retries for URL: " + request.url().toString());
    }

    private void detectOTPRequired(@Nonnull GitHubResponse.ResponseInfo responseInfo) throws GHIOException {
        // 401 Unauthorized == bad creds or OTP request
        if (responseInfo.statusCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            // In the case of a user with 2fa enabled, a header with X-GitHub-OTP
            // will be returned indicating the user needs to respond with an otp
            if (responseInfo.headerField("X-GitHub-OTP") != null) {
                throw new GHOTPRequiredException().withResponseHeaderFields(responseInfo.headers());
            }
        }
    }

    private boolean isRateLimitResponse(@Nonnull GitHubResponse.ResponseInfo responseInfo) {
        return responseInfo.statusCode() == HttpURLConnection.HTTP_FORBIDDEN
                && "0".equals(responseInfo.headerField("X-RateLimit-Remaining"));
    }

    private boolean isAbuseLimitResponse(@Nonnull GitHubResponse.ResponseInfo responseInfo) {
        return responseInfo.statusCode() == HttpURLConnection.HTTP_FORBIDDEN
                && responseInfo.headerField("Retry-After") != null;
    }

    private void handleLimitingErrors(@Nonnull GitHubResponse.ResponseInfo responseInfo) throws IOException {
        if (isRateLimitResponse(responseInfo)) {
            HttpException e = new HttpException("Rate limit violation",
                    responseInfo.statusCode(),
                    responseInfo.headerField("Status"),
                    responseInfo.url().toString());
            root.rateLimitHandler.onError(e, responseInfo.connection);
        } else if (isAbuseLimitResponse(responseInfo)) {
            HttpException e = new HttpException("Abuse limit violation",
                    responseInfo.statusCode(),
                    responseInfo.headerField("Status"),
                    responseInfo.url().toString());
            root.abuseLimitHandler.onError(e, responseInfo.connection);
        }
    }

    private boolean retryConnectionError(IOException e, URL url, int retries) throws IOException {
        // There are a range of connection errors where we want to wait a moment and just automatically retry
        boolean connectionError = e instanceof SocketException || e instanceof SocketTimeoutException
                || e instanceof SSLHandshakeException;
        if (connectionError && retries > 0) {
            LOGGER.log(INFO,
                    e.getMessage() + " while connecting to " + url + ". Sleeping " + Requester.retryTimeoutMillis
                            + " milliseconds before retrying... ; will try " + retries + " more time(s)");
            try {
                Thread.sleep(Requester.retryTimeoutMillis);
            } catch (InterruptedException ie) {
                throw (IOException) new InterruptedIOException().initCause(e);
            }
            return true;
        }
        return false;
    }

    private boolean isInvalidCached404Response(GitHubResponse.ResponseInfo responseInfo) {
        // WORKAROUND FOR ISSUE #669:
        // When the Requester detects a 404 response with an ETag (only happpens when the server's 304
        // is bogus and would cause cache corruption), try the query again with new request header
        // that forces the server to not return 304 and return new data instead.
        //
        // This solution is transparent to users of this library and automatically handles a
        // situation that was cause insidious and hard to debug bad responses in caching
        // scenarios. If GitHub ever fixes their issue and/or begins providing accurate ETags to
        // their 404 responses, this will result in at worst two requests being made for each 404
        // responses. However, only the second request will count against rate limit.
        if (responseInfo.statusCode() == 404 && Objects.equals(responseInfo.request().method(), "GET")
                && responseInfo.headerField("ETag") != null
                && !Objects.equals(responseInfo.request().headers().get("Cache-Control"), "no-cache")) {
            LOGGER.log(FINE,
                    "Encountered GitHub invalid cached 404 from " + responseInfo.url()
                            + ". Retrying with \"Cache-Control\"=\"no-cache\"...");

            // Setting "Cache-Control" to "no-cache" stops the cache from supplying
            // "If-Modified-Since" or "If-None-Match" values.
            // This makes GitHub give us current data (not incorrectly cached data)
            setHeader("Cache-Control", "no-cache");
            return true;
        }
        return false;
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

    private void noteRateLimit(@Nonnull GitHubResponse.ResponseInfo responseInfo) {
        if (responseInfo.request().urlPath().startsWith("/search")) {
            // the search API uses a different rate limit
            return;
        }

        String limitString = responseInfo.headerField("X-RateLimit-Limit");
        if (StringUtils.isBlank(limitString)) {
            // if we are missing a header, return fast
            return;
        }
        String remainingString = responseInfo.headerField("X-RateLimit-Remaining");
        if (StringUtils.isBlank(remainingString)) {
            // if we are missing a header, return fast
            return;
        }
        String resetString = responseInfo.headerField("X-RateLimit-Reset");
        if (StringUtils.isBlank(resetString)) {
            // if we are missing a header, return fast
            return;
        }

        int limit, remaining;
        long reset;
        try {
            limit = Integer.parseInt(limitString);
        } catch (NumberFormatException e) {
            if (LOGGER.isLoggable(FINEST)) {
                LOGGER.log(FINEST, "Malformed X-RateLimit-Limit header value " + limitString, e);
            }
            return;
        }
        try {

            remaining = Integer.parseInt(remainingString);
        } catch (NumberFormatException e) {
            if (LOGGER.isLoggable(FINEST)) {
                LOGGER.log(FINEST, "Malformed X-RateLimit-Remaining header value " + remainingString, e);
            }
            return;
        }
        try {
            reset = Long.parseLong(resetString);
        } catch (NumberFormatException e) {
            if (LOGGER.isLoggable(FINEST)) {
                LOGGER.log(FINEST, "Malformed X-RateLimit-Reset header value " + resetString, e);
            }
            return;
        }

        GHRateLimit.Record observed = new GHRateLimit.Record(limit, remaining, reset, responseInfo.headerField("Date"));

        root.updateCoreRateLimit(observed);
    }

    /**
     * Gets response header.
     *
     * @param header
     *            the header
     * @return the response header
     */
    public String getResponseHeader(String header) {
        return previousResponseInfo.headerField(header);
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
        if (pageSize > 0)
            this.with("per_page", pageSize);

        try {
            GitHubRequest request = build(root);
            if (!"GET".equals(request.method())) {
                throw new IllegalStateException("Request method \"GET\" is required for iterator.");
            }
            return new PagingIterator<>(type, request);
        } catch (IOException e) {
            throw new GHException("Unable to build github Api URL", e);
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
    class PagingIterator<T> implements Iterator<T> {

        private final Class<T> type;
        private GitHubRequest nextRequest;

        /**
         * The next batch to be returned from {@link #next()}.
         */
        private T next;

        PagingIterator(Class<T> type, GitHubRequest request) {
            this.type = type;
            this.nextRequest = request;
        }

        public boolean hasNext() {
            fetch();
            return next != null;
        }

        public T next() {
            fetch();
            T r = next;
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
                next = sendRequest(nextRequest, (responseInfo) -> {
                    T result = parse(responseInfo, type, null);
                    assert result != null;
                    findNextURL(responseInfo);
                    return result;
                }).body();
                assert next != null;

            } catch (IOException e) {
                throw new GHException("Failed to retrieve " + url, e);
            }
        }

        /**
         * Locate the next page from the pagination "Link" tag.
         */
        private void findNextURL(@Nonnull GitHubResponse.ResponseInfo responseInfo) throws MalformedURLException {
            nextRequest = null;
            String link = responseInfo.headerField("Link");
            if (link == null)
                return;

            for (String token : link.split(", ")) {
                if (token.endsWith("rel=\"next\"")) {
                    // found the next page. This should look something like
                    // <https://api.github.com/repos?page=3&per_page=100>; rel="next"
                    int idx = token.indexOf('>');
                    nextRequest = responseInfo.request().builder().build(root, new URL(token.substring(1, idx)));
                    return;
                }
            }

            // no more "next" link. we are done.
        }
    }

    static class GitHubClient {

    }

    @Nonnull
    static HttpURLConnection setupConnection(@Nonnull GitHub root, @Nonnull GitHubRequest request) throws IOException {
        if (LOGGER.isLoggable(FINE)) {
            LOGGER.log(FINE,
                    "GitHub API request [" + (root.login == null ? "anonymous" : root.login) + "]: " + request.method()
                            + " " + request.url().toString());
        }
        HttpURLConnection connection = root.getConnector().connect(request.url());

        // if the authentication is needed but no credential is given, try it anyway (so that some calls
        // that do work with anonymous access in the reduced form should still work.)
        if (root.encodedAuthorization != null)
            connection.setRequestProperty("Authorization", root.encodedAuthorization);

        setRequestMethod(request.method(), connection);
        buildRequest(request, connection);

        return connection;
    }

    /**
     * Set up the request parameters or POST payload.
     */
    private static void buildRequest(GitHubRequest request, HttpURLConnection connection) throws IOException {
        for (Map.Entry<String, String> e : request.headers().entrySet()) {
            String v = e.getValue();
            if (v != null)
                connection.setRequestProperty(e.getKey(), v);
        }
        connection.setRequestProperty("Accept-Encoding", "gzip");

        if (request.inBody()) {
            connection.setDoOutput(true);

            if (request.body() == null) {
                connection.setRequestProperty("Content-type", defaultString(request.contentType(), "application/json"));
                Map<String, Object> json = new HashMap<>();
                for (GitHubRequest.Entry e : request.args()) {
                    json.put(e.key, e.value);
                }
                MAPPER.writeValue(connection.getOutputStream(), json);
            } else {
                connection.setRequestProperty("Content-type",
                        defaultString(request.contentType(), "application/x-www-form-urlencoded"));
                try {
                    byte[] bytes = new byte[32768];
                    int read;
                    while ((read = request.body().read(bytes)) != -1) {
                        connection.getOutputStream().write(bytes, 0, read);
                    }
                } finally {
                    request.body().close();
                }
            }
        }
    }

    private static void setRequestMethod(String method, HttpURLConnection connection) throws IOException {
        try {
            connection.setRequestMethod(method);
        } catch (ProtocolException e) {
            // JDK only allows one of the fixed set of verbs. Try to override that
            try {
                Field $method = HttpURLConnection.class.getDeclaredField("method");
                $method.setAccessible(true);
                $method.set(connection, method);
            } catch (Exception x) {
                throw (IOException) new IOException("Failed to set the custom verb").initCause(x);
            }
            // sun.net.www.protocol.https.DelegatingHttpsURLConnection delegates to another HttpURLConnection
            try {
                Field $delegate = connection.getClass().getDeclaredField("delegate");
                $delegate.setAccessible(true);
                Object delegate = $delegate.get(connection);
                if (delegate instanceof HttpURLConnection) {
                    HttpURLConnection nested = (HttpURLConnection) delegate;
                    setRequestMethod(method, nested);
                }
            } catch (NoSuchFieldException x) {
                // no problem
            } catch (IllegalAccessException x) {
                throw (IOException) new IOException("Failed to set the custom verb").initCause(x);
            }
        }
        if (!connection.getRequestMethod().equals(method))
            throw new IllegalStateException("Failed to set the request method to " + method);
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

    /**
     * Handle API error by either throwing it or by returning normally to retry.
     */
    IOException interpretApiError(IOException e,
            @Nonnull GitHubRequest request,
            @CheckForNull GitHubResponse.ResponseInfo responseInfo) throws IOException {
        // If we're already throwing a GHIOException, pass through
        if (e instanceof GHIOException) {
            return e;
        }

        int statusCode = -1;
        String message = null;
        Map<String, List<String>> headers = new HashMap<>();
        InputStream es = null;

        if (responseInfo != null) {
            statusCode = responseInfo.statusCode();
            message = responseInfo.headerField("Status");
            headers = responseInfo.headers();
            es = responseInfo.wrapErrorStream();

        }

        if (es != null) {
            try {
                String error = IOUtils.toString(es, StandardCharsets.UTF_8);
                if (e instanceof FileNotFoundException) {
                    // pass through 404 Not Found to allow the caller to handle it intelligently
                    e = new GHFileNotFoundException(error, e).withResponseHeaderFields(headers);
                } else if (statusCode >= 0) {
                    e = new HttpException(error, statusCode, message, request.url().toString(), e);
                } else {
                    e = new GHIOException(error).withResponseHeaderFields(headers);
                }
            } finally {
                IOUtils.closeQuietly(es);
            }
        } else if (!(e instanceof FileNotFoundException)) {
            e = new HttpException(statusCode, message, request.url().toString(), e);
        }
        return e;
    }

    private static final Logger LOGGER = Logger.getLogger(Requester.class.getName());

    /**
     * Represents a supplier of results that can throw.
     *
     * <p>
     * This is a <a href="package-summary.html">functional interface</a> whose functional method is
     * {@link #apply(GitHubResponse.ResponseInfo)}.
     *
     * @param <T>
     *            the type of results supplied by this supplier
     */
    @FunctionalInterface
    interface ResponsBodyHandler<T> {

        /**
         * Gets a result.
         *
         * @return a result
         * @throws IOException
         */
        T apply(GitHubResponse.ResponseInfo input) throws IOException;
    }
}
