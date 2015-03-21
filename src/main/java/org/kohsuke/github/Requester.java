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

import static org.kohsuke.github.GitHub.MAPPER;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import com.fasterxml.jackson.databind.JsonMappingException;
import org.apache.commons.io.IOUtils;

import javax.net.ssl.HttpsURLConnection;

/**
 * A builder pattern for making HTTP call and parsing its output.
 *
 * @author Kohsuke Kawaguchi
 */
class Requester {
    private final GitHub root;
    private final List<Entry> args = new ArrayList<Entry>();

    /**
     * Request method.
     */
    private String method = "POST";
    private String contentType = "application/x-www-form-urlencoded";
    private InputStream body;

    private static class Entry {
        String key;
        Object value;

        private Entry(String key, Object value) {
            this.key = key;
            this.value = value;
        }
    }

    Requester(GitHub root) {
        this.root = root;
    }

    /**
     * Makes a request with authentication credential.
     */
    @Deprecated
    public Requester withCredential() {
        // keeping it inline with retrieveWithAuth not to enforce the check
        // root.requireCredential();
        return this;
    }

    public Requester with(String key, int value) {
        return _with(key, value);
    }

    public Requester with(String key, Integer value) {
        if (value!=null)
            _with(key, value.intValue());
        return this;
    }

    public Requester with(String key, boolean value) {
        return _with(key, value);
    }

    public Requester with(String key, String value) {
        return _with(key, value);
    }

    public Requester with(String key, Collection<String> value) {
        return _with(key, value);
    }

    public Requester with(String key, Map<String, String> value) {
        return _with(key, value);
    }

    public Requester with(InputStream body) {
        this.body = body;
        return this;
    }

    public Requester _with(String key, Object value) {
        if (value!=null) {
            args.add(new Entry(key,value));
        }
        return this;
    }

    /**
     * Unlike {@link #with(String, String)}, overrides the existing value
     */
    public Requester set(String key, Object value) {
        for (Entry e : args) {
            if (e.key.equals(key)) {
                e.value = value;
                return this;
            }
        }
        return _with(key,value);
    }

    public Requester method(String method) {
        this.method = method;
        return this;
    }

    public Requester contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public void to(String tailApiUrl) throws IOException {
        to(tailApiUrl,null);
    }

    /**
     * Sends a request to the specified URL, and parses the response into the given type via databinding.
     *
     * @throws IOException
     *      if the server returns 4xx/5xx responses.
     * @return
     *      {@link Reader} that reads the response.
     */
    public <T> T to(String tailApiUrl, Class<T> type) throws IOException {
        return _to(tailApiUrl, type, null);
    }

    /**
     * Like {@link #to(String, Class)} but updates an existing object instead of creating a new instance.
     */
    public <T> T to(String tailApiUrl, T existingInstance) throws IOException {
        return _to(tailApiUrl, null, existingInstance);
    }

    /**
     * Short for {@code method(method).to(tailApiUrl,type)}
     */
    @Deprecated
    public <T> T to(String tailApiUrl, Class<T> type, String method) throws IOException {
        return method(method).to(tailApiUrl,type);
    }

    private <T> T _to(String tailApiUrl, Class<T> type, T instance) throws IOException {
        while (true) {// loop while API rate limit is hit
            if (method.equals("GET") && !args.isEmpty()) {
                StringBuilder qs=new StringBuilder();
                for (Entry arg : args) {
                    qs.append(qs.length()==0 ? '?' : '&');
                    qs.append(arg.key).append('=').append(URLEncoder.encode(arg.value.toString(),"UTF-8"));
                }
                tailApiUrl += qs.toString();
            }
            HttpURLConnection uc = setupConnection(root.getApiURL(tailApiUrl));

            buildRequest(uc);

            try {
                T result = parse(uc, type, instance);
                if (type != null && type.isArray()) { // we might have to loop for pagination - done through recursion
                    final String links = uc.getHeaderField("link");
                    if (links != null && links.contains("rel=\"next\"")) {
                        Pattern nextLinkPattern = Pattern.compile(".*<(.*)>; rel=\"next\"");
                        Matcher nextLinkMatcher = nextLinkPattern.matcher(links);
                        if (nextLinkMatcher.find()) {
                            final String link = nextLinkMatcher.group(1);
                            T nextResult = _to(link, type, instance);

                            final int resultLength = Array.getLength(result);
                            final int nextResultLength = Array.getLength(nextResult);
                            T concatResult = (T) Array.newInstance(type.getComponentType(), resultLength + nextResultLength);
                            System.arraycopy(result, 0, concatResult, 0, resultLength);
                            System.arraycopy(nextResult, 0, concatResult, resultLength, nextResultLength);
                            result = concatResult;
                        }
                    }
                }
                return result;
            } catch (IOException e) {
                handleApiError(e,uc);
            }
        }
    }

    /**
     * Makes a request and just obtains the HTTP status code.
     */
    public int asHttpStatusCode(String tailApiUrl) throws IOException {
        while (true) {// loop while API rate limit is hit
            HttpURLConnection uc = setupConnection(root.getApiURL(tailApiUrl));

            buildRequest(uc);

            try {
                return uc.getResponseCode();
            } catch (IOException e) {
                handleApiError(e,uc);
            }
        }
    }

    private void buildRequest(HttpURLConnection uc) throws IOException {
        if (!method.equals("GET")) {
            uc.setDoOutput(true);
            uc.setRequestProperty("Content-type", contentType);

            if (body == null) {
                Map json = new HashMap();
                for (Entry e : args) {
                    json.put(e.key, e.value);
                }
                MAPPER.writeValue(uc.getOutputStream(), json);
            } else {
                try {
                    byte[] bytes = new byte[32768];
                    int read = 0;
                    while ((read = body.read(bytes)) != -1) {
                        uc.getOutputStream().write(bytes, 0, read);
                    }
                } finally {
                    body.close();
                }
            }
        }
    }

    /**
     * Loads pagenated resources.
     *
     * Every iterator call reports a new batch.
     */
    /*package*/ <T> Iterator<T> asIterator(String _tailApiUrl, final Class<T> type) {
        method("GET");

        if (!args.isEmpty()) {
            boolean first=true;
            try {
                for (Entry a : args) {
                    _tailApiUrl += first ? '?' : '&';
                    first = false;
                    _tailApiUrl += URLEncoder.encode(a.key,"UTF-8")+'='+URLEncoder.encode(a.value.toString(),"UTF-8");
                }
            } catch (UnsupportedEncodingException e) {
                throw new AssertionError(e);    // UTF-8 is mandatory
            }
        }

        final String tailApiUrl = _tailApiUrl;

        return new Iterator<T>() {
            /**
             * The next batch to be returned from {@link #next()}.
             */
            T next;
            /**
             * URL of the next resource to be retrieved, or null if no more data is available.
             */
            URL url;

            {
                try {
                    url = root.getApiURL(tailApiUrl);
                } catch (IOException e) {
                    throw new Error(e);
                }
            }

            public boolean hasNext() {
                fetch();
                return next!=null;
            }

            public T next() {
                fetch();
                T r = next;
                if (r==null)    throw new NoSuchElementException();
                next = null;
                return r;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

            private void fetch() {
                if (next!=null) return; // already fetched
                if (url==null)  return; // no more data to fetch

                try {
                    while (true) {// loop while API rate limit is hit
                        HttpURLConnection uc = setupConnection(url);
                        try {
                            next = parse(uc,type,null);
                            assert next!=null;
                            findNextURL(uc);
                            return;
                        } catch (IOException e) {
                            handleApiError(e,uc);
                        }
                    }
                } catch (IOException e) {
                    throw new Error(e);
                }
            }

            /**
             * Locate the next page from the pagination "Link" tag.
             */
            private void findNextURL(HttpURLConnection uc) throws MalformedURLException {
                url = null; // start defensively
                String link = uc.getHeaderField("Link");
                if (link==null) return;

                for (String token : link.split(", ")) {
                    if (token.endsWith("rel=\"next\"")) {
                        // found the next page. This should look something like
                        // <https://api.github.com/repos?page=3&per_page=100>; rel="next"
                        int idx = token.indexOf('>');
                        url = new URL(token.substring(1,idx));
                        return;
                    }
                }

                // no more "next" link. we are done.
            }
        };
    }


    private HttpURLConnection setupConnection(URL url) throws IOException {
        HttpURLConnection uc = root.getConnector().connect(url);

        // if the authentication is needed but no credential is given, try it anyway (so that some calls
        // that do work with anonymous access in the reduced form should still work.)
        if (root.encodedAuthorization!=null)
            uc.setRequestProperty("Authorization", root.encodedAuthorization);

        try {
            uc.setRequestMethod(method);
        } catch (ProtocolException e) {
            // JDK only allows one of the fixed set of verbs. Try to override that
            try {
                Field $method = HttpURLConnection.class.getDeclaredField("method");
                $method.setAccessible(true);
                $method.set(uc,method);
            } catch (Exception x) {
                throw (IOException)new IOException("Failed to set the custom verb").initCause(x);
            }
        }
        uc.setRequestProperty("Accept-Encoding", "gzip");
        return uc;
    }

    private <T> T parse(HttpURLConnection uc, Class<T> type, T instance) throws IOException {
        InputStreamReader r = null;
        try {
            r = new InputStreamReader(wrapStream(uc, uc.getInputStream()), "UTF-8");
            String data = IOUtils.toString(r);
            if (type!=null)
                try {
                    return MAPPER.readValue(data,type);
                } catch (JsonMappingException e) {
                    throw (IOException)new IOException("Failed to deserialize "+data).initCause(e);
                }
            if (instance!=null)
                return MAPPER.readerForUpdating(instance).<T>readValue(data);
            return null;
        } finally {
            IOUtils.closeQuietly(r);
        }
    }

    /**
     * Handles the "Content-Encoding" header.
     */
    private InputStream wrapStream(HttpURLConnection uc, InputStream in) throws IOException {
        String encoding = uc.getContentEncoding();
        if (encoding==null || in==null) return in;
        if (encoding.equals("gzip"))    return new GZIPInputStream(in);

        throw new UnsupportedOperationException("Unexpected Content-Encoding: "+encoding);
    }

    /**
     * Handle API error by either throwing it or by returning normally to retry.
     */
    /*package*/ void handleApiError(IOException e, HttpURLConnection uc) throws IOException {
        if ("0".equals(uc.getHeaderField("X-RateLimit-Remaining"))) {
            root.rateLimitHandler.onError(e,uc);
        }

        if (e instanceof FileNotFoundException)
            throw e;    // pass through 404 Not Found to allow the caller to handle it intelligently

        InputStream es = wrapStream(uc, uc.getErrorStream());
        try {
            if (es!=null)
                throw (IOException)new IOException(IOUtils.toString(es,"UTF-8")).initCause(e);
            else
                throw e;
        } finally {
            IOUtils.closeQuietly(es);
        }
    }

    private Set<String> toSet(String s) {
        Set<String> r = new HashSet<String>();
        for (String t : s.split(","))
            r.add(t.trim());
        return r;
    }
}
