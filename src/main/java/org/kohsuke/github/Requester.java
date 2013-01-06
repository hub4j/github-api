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

import org.apache.commons.io.IOUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.zip.GZIPInputStream;

import static org.kohsuke.github.GitHub.*;

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

    public Requester _with(String key, Object value) {
        if (value!=null) {
            args.add(new Entry(key,value));
        }
        return this;
    }

    public Requester method(String method) {
        this.method = method;
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
            HttpURLConnection uc = setupConnection(root.getApiURL(tailApiUrl));

            if (!method.equals("GET")) {
                uc.setDoOutput(true);
                uc.setRequestProperty("Content-type","application/x-www-form-urlencoded");

                Map json = new HashMap();
                for (Entry e : args) {
                    json.put(e.key, e.value);
                }
                MAPPER.writeValue(uc.getOutputStream(),json);
            }

            try {
                return parse(uc,type,instance);
            } catch (IOException e) {
                handleApiError(e,uc);
            }
        }
    }

    /**
     * Loads pagenated resources.
     *
     * Every iterator call reports a new batch.
     */
    /*package*/ <T> Iterator<T> asIterator(final String tailApiUrl, final Class<T> type) {
        method("GET");
        if (!args.isEmpty())    throw new IllegalStateException();

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
        HttpURLConnection uc = (HttpURLConnection) url.openConnection();

        // if the authentication is needed but no credential is given, try it anyway (so that some calls
        // that do work with anonymous access in the reduced form should still work.)
        // if OAuth token is present, it'll be set in the URL, so need to set the Authorization header
        if (root.encodedAuthorization!=null && root.oauthAccessToken == null)
            uc.setRequestProperty("Authorization", "Basic " + root.encodedAuthorization);

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
                return MAPPER.readValue(data,type);
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
     * If the error is because of the API limit, wait 10 sec and return normally.
     * Otherwise throw an exception reporting an error.
     */
    /*package*/ void handleApiError(IOException e, HttpURLConnection uc) throws IOException {
        if ("0".equals(uc.getHeaderField("X-RateLimit-Remaining"))) {
            // API limit reached. wait 10 secs and return normally
            try {
                Thread.sleep(10000);
                return;
            } catch (InterruptedException _) {
                throw (InterruptedIOException)new InterruptedIOException().initCause(e);
            }
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
}
