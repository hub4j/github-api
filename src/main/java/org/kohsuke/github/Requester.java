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
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.WillClose;

import static java.util.Arrays.asList;
import static java.util.logging.Level.*;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.kohsuke.github.GitHub.MAPPER;

/**
 * A builder pattern for making HTTP call and parsing its output.
 *
 * @author Kohsuke Kawaguchi
 */
class Requester {
    private final GitHub root;
    private final List<Entry> args = new ArrayList<Entry>();
    private final Map<String, String> headers = new LinkedHashMap<String, String>();

    @Nonnull
    private String urlPath = "/";

    /**
     * Request method.
     */
    private String method = "GET";
    private String contentType = null;
    private InputStream body;

    /**
     * Current connection.
     */
    private HttpURLConnection uc;
    private boolean forceBody;

    private static class Entry {
        final String key;
        final Object value;

        private Entry(String key, Object value) {
            this.key = key;
            this.value = value;
        }
    }

    Requester(GitHub root) {
        this.root = root;
    }

    /**
     * Sets the request HTTP header.
     * <p>
     * If a header of the same name is already set, this method overrides it.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     */
    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    /**
     * With header requester.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     * @return the requester
     */
    public Requester withHeader(String name, String value) {
        setHeader(name, value);
        return this;
    }

    public Requester withPreview(String name) {
        return withHeader("Accept", name);
    }

    /**
     * With requester.
     *
     * @param key
     *            the key
     * @param value
     *            the value
     * @return the requester
     */
    public Requester with(String key, int value) {
        return with(key, (Object) value);
    }

    /**
     * With requester.
     *
     * @param key
     *            the key
     * @param value
     *            the value
     * @return the requester
     */
    public Requester with(String key, long value) {
        return with(key, (Object) value);
    }

    /**
     * With requester.
     *
     * @param key
     *            the key
     * @param value
     *            the value
     * @return the requester
     */
    public Requester with(String key, boolean value) {
        return with(key, (Object) value);
    }

    /**
     * With requester.
     *
     * @param key
     *            the key
     * @param e
     *            the e
     * @return the requester
     */
    public Requester with(String key, Enum e) {
        if (e == null)
            return with(key, (Object) null);
        return with(key, transformEnum(e));
    }

    /**
     * With requester.
     *
     * @param key
     *            the key
     * @param value
     *            the value
     * @return the requester
     */
    public Requester with(String key, String value) {
        return with(key, (Object) value);
    }

    /**
     * With requester.
     *
     * @param key
     *            the key
     * @param value
     *            the value
     * @return the requester
     */
    public Requester with(String key, Collection<?> value) {
        return with(key, (Object) value);
    }

    /**
     * With requester.
     *
     * @param key
     *            the key
     * @param value
     *            the value
     * @return the requester
     */
    public Requester with(String key, Map<String, String> value) {
        return with(key, (Object) value);
    }

    /**
     * With requester.
     *
     * @param body
     *            the body
     * @return the requester
     */
    public Requester with(@WillClose /* later */ InputStream body) {
        this.body = body;
        return this;
    }

    /**
     * With nullable requester.
     *
     * @param key
     *            the key
     * @param value
     *            the value
     * @return the requester
     */
    public Requester withNullable(String key, Object value) {
        args.add(new Entry(key, value));
        return this;
    }

    /**
     * With requester.
     *
     * @param key
     *            the key
     * @param value
     *            the value
     * @return the requester
     */
    public Requester with(String key, Object value) {
        if (value != null) {
            args.add(new Entry(key, value));
        }
        return this;
    }

    /**
     * Unlike {@link #with(String, String)}, overrides the existing value
     *
     * @param key
     *            the key
     * @param value
     *            the value
     * @return the requester
     */
    public Requester set(String key, Object value) {
        for (int index = 0; index < args.size(); index++) {
            if (args.get(index).key.equals(key)) {
                args.set(index, new Entry(key, value));
                return this;
            }
        }
        return with(key, value);
    }

    /**
     * Method requester.
     *
     * @param method
     *            the method
     * @return the requester
     */
    public Requester method(String method) {
        this.method = method;
        return this;
    }

    /**
     * Content type requester.
     *
     * @param contentType
     *            the content type
     * @return the requester
     */
    public Requester contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * NOT FOR PUBLIC USE. Do not make this method public.
     * <p>
     * Sets the path component of api URL without URI encoding.
     * <p>
     * Should only be used when passing a literal URL field from a GHObject, such as {@link GHContent#refresh()} or when
     * needing to set query parameters on requests methods that don't usually have them, such as
     * {@link GHRelease#uploadAsset(String, InputStream, String)}.
     *
     * @param urlOrPath
     *            the content type
     * @return the requester
     */
    Requester setRawUrlPath(String urlOrPath) {
        Objects.requireNonNull(urlOrPath);
        this.urlPath = urlOrPath;
        return this;
    }

    /**
     * Path component of api URL. Appended to api url.
     * <p>
     * If urlPath starts with a slash, it will be URI encoded as a path. If it starts with anything else, it will be
     * used as is.
     *
     * @param urlPathItems
     *            the content type
     * @return the requester
     */
    public Requester withUrlPath(String... urlPathItems) {
        if (!this.urlPath.startsWith("/")) {
            throw new GHException("Cannot append to url path after setting a raw path");
        }

        if (urlPathItems.length == 1 && !urlPathItems[0].startsWith("/")) {
            return setRawUrlPath(urlPathItems[0]);
        }

        String tailUrlPath = String.join("/", urlPathItems);

        if (this.urlPath.endsWith("/")) {
            tailUrlPath = StringUtils.stripStart(tailUrlPath, "/");
        } else {
            tailUrlPath = StringUtils.prependIfMissing(tailUrlPath, "/");
        }

        this.urlPath += urlPathEncode(tailUrlPath);
        return this;
    }

    /**
     * Small number of GitHub APIs use HTTP methods somewhat inconsistently, and use a body where it's not expected.
     * Normally whether parameters go as query parameters or a body depends on the HTTP verb in use, but this method
     * forces the parameters to be sent as a body.
     */
    public Requester inBody() {
        forceBody = true;
        return this;
    }

    /**
     * To.
     *
     * @throws IOException
     *             the io exception
     */
    public void to() throws IOException {
        _to(null, null);
    }

    /**
     * Sends a request to the specified URL, and parses the response into the given type via databinding.
     *
     * @param <T>
     *            the type parameter
     * @param type
     *            the type
     * @return {@link Reader} that reads the response.
     * @throws IOException
     *             if the server returns 4xx/5xx responses.
     */
    public <T> T to(@Nonnull Class<T> type) throws IOException {
        return _to(type, null);
    }

    /**
     * Sends a request to the specified URL, and parses the response into the given type via databinding.
     *
     * @param <T>
     *            the type parameter
     * @param type
     *            the type
     * @return {@link Reader} that reads the response.
     * @throws IOException
     *             if the server returns 4xx/5xx responses.
     */
    public <T> T[] toArray(@Nonnull Class<T[]> type) throws IOException {
        T[] result;

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
        return setResponseHeaders(result);
    }

    /**
     * Like {@link #to(Class)} but updates an existing object instead of creating a new instance.
     *
     * @param <T>
     *            the type parameter
     * @param existingInstance
     *            the existing instance
     * @return the t
     * @throws IOException
     *             the io exception
     */
    public <T> T to(@Nonnull T existingInstance) throws IOException {
        return _to(null, existingInstance);
    }

    private <T> T _to(Class<T> type, T instance) throws IOException {
        T result;

        String tailApiUrl = buildTailApiUrl(urlPath);
        setupConnection(root.getApiURL(tailApiUrl));

        while (true) {// loop while API rate limit is hit
            try {
                result = parse(type, instance);
                return setResponseHeaders(result);
            } catch (IOException e) {
                handleApiError(e);
            } finally {
                noteRateLimit(tailApiUrl);
            }
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

    private String buildTailApiUrl(String tailApiUrl) {
        if (!isMethodWithBody() && !args.isEmpty()) {
            try {
                boolean questionMarkFound = tailApiUrl.indexOf('?') != -1;
                StringBuilder argString = new StringBuilder();
                argString.append(questionMarkFound ? '&' : '?');

                for (Iterator<Entry> it = args.listIterator(); it.hasNext();) {
                    Entry arg = it.next();
                    argString.append(URLEncoder.encode(arg.key, "UTF-8"));
                    argString.append('=');
                    argString.append(URLEncoder.encode(arg.value.toString(), "UTF-8"));
                    if (it.hasNext()) {
                        argString.append('&');
                    }
                }
                tailApiUrl += argString;
            } catch (UnsupportedEncodingException e) {
                throw new AssertionError(e); // UTF-8 is mandatory
            }
        }
        return tailApiUrl;
    }

    /**
     * Makes a request and just obtains the HTTP status code. Method does not throw exceptions for many status codes
     * that would otherwise throw.
     *
     * @return the int
     * @throws IOException
     *             the io exception
     */
    public int asHttpStatusCode() throws IOException {
        while (true) {// loop while API rate limit is hit

            setupConnection(root.getApiURL(urlPath));

            try {
                return uc.getResponseCode();
            } catch (IOException e) {
                handleApiError(e);
            } finally {
                noteRateLimit(urlPath);
            }
        }
    }

    /**
     * As stream input stream.
     *
     * @return the input stream
     * @throws IOException
     *             the io exception
     */
    public InputStream toStream() throws IOException {
        while (true) {// loop while API rate limit is hit
            setupConnection(root.getApiURL(urlPath));

            try {
                return wrapStream(uc.getInputStream());
            } catch (IOException e) {
                handleApiError(e);
            } finally {
                noteRateLimit(urlPath);
            }
        }
    }

    private void noteRateLimit(String tailApiUrl) {
        if (uc == null) {
            return;
        }
        if (tailApiUrl.startsWith("/search")) {
            // the search API uses a different rate limit
            return;
        }

        String limitString = uc.getHeaderField("X-RateLimit-Limit");
        if (StringUtils.isBlank(limitString)) {
            // if we are missing a header, return fast
            return;
        }
        String remainingString = uc.getHeaderField("X-RateLimit-Remaining");
        if (StringUtils.isBlank(remainingString)) {
            // if we are missing a header, return fast
            return;
        }
        String resetString = uc.getHeaderField("X-RateLimit-Reset");
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

        GHRateLimit.Record observed = new GHRateLimit.Record(limit, remaining, reset, uc.getHeaderField("Date"));

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
        return uc.getHeaderField(header);
    }

    /**
     * Set up the request parameters or POST payload.
     */
    private void buildRequest() throws IOException {
        if (isMethodWithBody()) {
            uc.setDoOutput(true);

            if (body == null) {
                uc.setRequestProperty("Content-type", defaultString(contentType, "application/json"));
                Map json = new HashMap();
                for (Entry e : args) {
                    json.put(e.key, e.value);
                }
                MAPPER.writeValue(uc.getOutputStream(), json);
            } else {
                uc.setRequestProperty("Content-type", defaultString(contentType, "application/x-www-form-urlencoded"));
                try {
                    byte[] bytes = new byte[32768];
                    int read;
                    while ((read = body.read(bytes)) != -1) {
                        uc.getOutputStream().write(bytes, 0, read);
                    }
                } finally {
                    body.close();
                }
            }
        }
    }

    private boolean isMethodWithBody() {
        return forceBody || !METHODS_WITHOUT_BODY.contains(method);
    }

    <T> PagedIterable<T> asPagedIterable(String tailApiUrl, Class<T[]> type, Consumer<T> consumer) {
        return withUrlPath(tailApiUrl).asPagedIterable(type, consumer);
    }

    <T> PagedIterable<T> asPagedIterable(Class<T[]> type, Consumer<T> consumer) {
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
     * Every iterator call reports a new batch.
     */
    <T> Iterator<T> asIterator(Class<T> type, int pageSize) {
        if (method != "GET") {
            throw new IllegalStateException("Request method \"GET\" is required for iterator.");
        }

        if (pageSize != 0)
            args.add(new Entry("per_page", pageSize));

        String tailApiUrl = buildTailApiUrl(urlPath);

        try {
            return new PagingIterator<>(type, tailApiUrl, root.getApiURL(tailApiUrl));
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
        private final String tailApiUrl;

        /**
         * The next batch to be returned from {@link #next()}.
         */
        private T next;

        /**
         * URL of the next resource to be retrieved, or null if no more data is available.
         */
        private URL url;

        PagingIterator(Class<T> type, String tailApiUrl, URL url) {
            this.type = type;
            this.tailApiUrl = tailApiUrl;
            this.url = url;
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
            if (url == null)
                return; // no more data to fetch

            try {
                while (true) {// loop while API rate limit is hit
                    setupConnection(url);
                    try {
                        next = parse(type, null);
                        assert next != null;
                        findNextURL();
                        return;
                    } catch (IOException e) {
                        handleApiError(e);
                    } finally {
                        noteRateLimit(tailApiUrl);
                    }
                }
            } catch (IOException e) {
                throw new GHException("Failed to retrieve " + url, e);
            }
        }

        /**
         * Locate the next page from the pagination "Link" tag.
         */
        private void findNextURL() throws MalformedURLException {
            url = null; // start defensively
            String link = uc.getHeaderField("Link");
            if (link == null)
                return;

            for (String token : link.split(", ")) {
                if (token.endsWith("rel=\"next\"")) {
                    // found the next page. This should look something like
                    // <https://api.github.com/repos?page=3&per_page=100>; rel="next"
                    int idx = token.indexOf('>');
                    url = new URL(token.substring(1, idx));
                    return;
                }
            }

            // no more "next" link. we are done.
        }
    }

    private void setupConnection(URL url) throws IOException {
        if (LOGGER.isLoggable(FINE)) {
            LOGGER.log(FINE,
                    "GitHub API request [" + (root.login == null ? "anonymous" : root.login) + "]: " + method + " "
                            + url.toString());
        }
        uc = root.getConnector().connect(url);

        // if the authentication is needed but no credential is given, try it anyway (so that some calls
        // that do work with anonymous access in the reduced form should still work.)
        if (root.encodedAuthorization != null)
            uc.setRequestProperty("Authorization", root.encodedAuthorization);

        for (Map.Entry<String, String> e : headers.entrySet()) {
            String v = e.getValue();
            if (v != null)
                uc.setRequestProperty(e.getKey(), v);
        }

        setRequestMethod(uc);
        uc.setRequestProperty("Accept-Encoding", "gzip");
        buildRequest();
    }

    private void setRequestMethod(HttpURLConnection uc) throws IOException {
        try {
            uc.setRequestMethod(method);
        } catch (ProtocolException e) {
            // JDK only allows one of the fixed set of verbs. Try to override that
            try {
                Field $method = HttpURLConnection.class.getDeclaredField("method");
                $method.setAccessible(true);
                $method.set(uc, method);
            } catch (Exception x) {
                throw (IOException) new IOException("Failed to set the custom verb").initCause(x);
            }
            // sun.net.www.protocol.https.DelegatingHttpsURLConnection delegates to another HttpURLConnection
            try {
                Field $delegate = uc.getClass().getDeclaredField("delegate");
                $delegate.setAccessible(true);
                Object delegate = $delegate.get(uc);
                if (delegate instanceof HttpURLConnection) {
                    HttpURLConnection nested = (HttpURLConnection) delegate;
                    setRequestMethod(nested);
                }
            } catch (NoSuchFieldException x) {
                // no problem
            } catch (IllegalAccessException x) {
                throw (IOException) new IOException("Failed to set the custom verb").initCause(x);
            }
        }
        if (!uc.getRequestMethod().equals(method))
            throw new IllegalStateException("Failed to set the request method to " + method);
    }

    @CheckForNull
    private <T> T parse(Class<T> type, T instance) throws IOException {
        return parse(type, instance, 2);
    }

    private <T> T parse(Class<T> type, T instance, int timeouts) throws IOException {
        InputStreamReader r = null;
        int responseCode = -1;
        String responseMessage = null;
        try {
            responseCode = uc.getResponseCode();
            responseMessage = uc.getResponseMessage();
            if (responseCode == 304) {
                return null; // special case handling for 304 unmodified, as the content will be ""
            }
            if (responseCode == 204 && type != null && type.isArray()) {
                // no content
                return type.cast(Array.newInstance(type.getComponentType(), 0));
            }

            // Response code 202 means data is being generated still being cached.
            // This happens in for statistics:
            // See https://developer.github.com/v3/repos/statistics/#a-word-about-caching
            // And for fork creation:
            // See https://developer.github.com/v3/repos/forks/#create-a-fork
            if (responseCode == 202) {
                if (uc.getURL().toString().endsWith("/forks")) {
                    LOGGER.log(INFO, "The fork is being created. Please try again in 5 seconds.");
                } else if (uc.getURL().toString().endsWith("/statistics")) {
                    LOGGER.log(INFO, "The statistics are being generated. Please try again in 5 seconds.");
                } else {
                    LOGGER.log(INFO,
                            "Received 202 from " + uc.getURL().toString() + " . Please try again in 5 seconds.");
                }
                // Maybe throw an exception instead?
                return null;
            }

            r = new InputStreamReader(wrapStream(uc.getInputStream()), "UTF-8");
            String data = IOUtils.toString(r);
            if (type != null)
                try {
                    return setResponseHeaders(MAPPER.readValue(data, type));
                } catch (JsonMappingException e) {
                    throw (IOException) new IOException("Failed to deserialize " + data).initCause(e);
                }
            if (instance != null) {
                return setResponseHeaders(MAPPER.readerForUpdating(instance).<T>readValue(data));
            }
            return null;
        } catch (FileNotFoundException e) {
            // java.net.URLConnection handles 404 exception has FileNotFoundException, don't wrap exception in
            // HttpException
            // to preserve backward compatibility
            throw e;
        } catch (IOException e) {
            if (e instanceof SocketTimeoutException && timeouts > 0) {
                LOGGER.log(INFO, "timed out accessing " + uc.getURL() + "; will try " + timeouts + " more time(s)", e);
                return parse(type, instance, timeouts - 1);
            }
            throw new HttpException(responseCode, responseMessage, uc.getURL(), e);
        } finally {
            IOUtils.closeQuietly(r);
        }
    }

    private <T> T setResponseHeaders(T readValue) {
        if (readValue instanceof GHObject[]) {
            for (GHObject ghObject : (GHObject[]) readValue) {
                setResponseHeaders(ghObject);
            }
        } else if (readValue instanceof GHObject) {
            setResponseHeaders((GHObject) readValue);
        } else if (readValue instanceof JsonRateLimit) {
            // if we're getting a GHRateLimit it needs the server date
            ((JsonRateLimit) readValue).resources.getCore().recalculateResetDate(uc.getHeaderField("Date"));
        }
        return readValue;
    }

    private void setResponseHeaders(GHObject readValue) {
        readValue.responseHeaderFields = uc.getHeaderFields();
    }

    /**
     * Handles the "Content-Encoding" header.
     */
    private InputStream wrapStream(InputStream in) throws IOException {
        String encoding = uc.getContentEncoding();
        if (encoding == null || in == null)
            return in;
        if (encoding.equals("gzip"))
            return new GZIPInputStream(in);

        throw new UnsupportedOperationException("Unexpected Content-Encoding: " + encoding);
    }

    /**
     * Handle API error by either throwing it or by returning normally to retry.
     */
    void handleApiError(IOException e) throws IOException {
        int responseCode;
        try {
            responseCode = uc.getResponseCode();
        } catch (IOException e2) {
            // likely to be a network exception (e.g. SSLHandshakeException),
            // uc.getResponseCode() and any other getter on the response will cause an exception
            if (LOGGER.isLoggable(FINE))
                LOGGER.log(FINE,
                        "Silently ignore exception retrieving response code for '" + uc.getURL() + "'"
                                + " handling exception " + e,
                        e);
            throw e;
        }
        InputStream es = wrapStream(uc.getErrorStream());
        if (es != null) {
            try {
                String error = IOUtils.toString(es, "UTF-8");
                if (e instanceof FileNotFoundException) {
                    // pass through 404 Not Found to allow the caller to handle it intelligently
                    e = (IOException) new GHFileNotFoundException(error).withResponseHeaderFields(uc).initCause(e);
                } else if (e instanceof HttpException) {
                    HttpException http = (HttpException) e;
                    e = new HttpException(error, http.getResponseCode(), http.getResponseMessage(), http.getUrl(), e);
                } else {
                    e = (IOException) new GHIOException(error).withResponseHeaderFields(uc).initCause(e);
                }
            } finally {
                IOUtils.closeQuietly(es);
            }
        }
        if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) // 401 Unauthorized == bad creds or OTP request
            // In the case of a user with 2fa enabled, a header with X-GitHub-OTP
            // will be returned indicating the user needs to respond with an otp
            if (uc.getHeaderField("X-GitHub-OTP") != null)
                throw (IOException) new GHOTPRequiredException().withResponseHeaderFields(uc).initCause(e);
            else
                throw e; // usually org.kohsuke.github.HttpException (which extends IOException)

        if ("0".equals(uc.getHeaderField("X-RateLimit-Remaining"))) {
            root.rateLimitHandler.onError(e, uc);
            return;
        }

        // Retry-After is not documented but apparently that field exists
        if (responseCode == HttpURLConnection.HTTP_FORBIDDEN && uc.getHeaderField("Retry-After") != null) {
            this.root.abuseLimitHandler.onError(e, uc);
            return;
        }

        throw e;
    }

    /**
     * Transform Java Enum into Github constants given its conventions
     * 
     * @param en
     *            Enum to be transformed
     * @return a String containing the value of a Github constant
     */
    static String transformEnum(Enum en) {
        // by convention Java constant names are upper cases, but github uses
        // lower-case constants. GitHub also uses '-', which in Java we always
        // replace by '_'
        return en.toString().toLowerCase(Locale.ENGLISH).replace('_', '-');
    }

    /**
     * Encode the path to url safe string.
     *
     * @param value
     *            string to be path encoded.
     * @return The encoded string.
     */
    public static String urlPathEncode(String value) {
        try {
            return new URI(null, null, value, null, null).toString();
        } catch (URISyntaxException ex) {
            throw new AssertionError(ex);
        }
    }

    private static final List<String> METHODS_WITHOUT_BODY = asList("GET", "DELETE");
    private static final Logger LOGGER = Logger.getLogger(Requester.class.getName());
}
