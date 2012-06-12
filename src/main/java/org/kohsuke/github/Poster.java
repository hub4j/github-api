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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.kohsuke.github.GitHub.*;

/**
 * Handles HTTP POST.
 * @author Kohsuke Kawaguchi
 */
class Poster {
    private final GitHub root;
    private final List<Entry> args = new ArrayList<Entry>();
    private boolean authenticate;

    private static class Entry {
        String key;
        Object value;

        private Entry(String key, Object value) {
            this.key = key;
            this.value = value;
        }
    }

    Poster(GitHub root) {
        this.root = root;
    }

    public Poster withCredential() {
        root.requireCredential();
        authenticate = true;
        return this;
    }

    public Poster with(String key, int value) {
        return _with(key, value);
    }

    public Poster with(String key, Integer value) {
        if (value!=null)
            _with(key, value.intValue());
        return this;
    }

    public Poster with(String key, boolean value) {
        return _with(key, value);
    }

    public Poster with(String key, String value) {
        return _with(key, value);
    }

    public Poster with(String key, Collection<String> value) {
        return _with(key, value);
    }

    public Poster _with(String key, Object value) {
        if (value!=null) {
            args.add(new Entry(key,value));
        }
        return this;
    }

    public void to(String tailApiUrl) throws IOException {
        to(tailApiUrl,null);
    }

    /**
     * POSTs the form to the specified URL.
     *
     * @throws IOException
     *      if the server returns 4xx/5xx responses.
     * @return
     *      {@link Reader} that reads the response.
     */
    public <T> T to(String tailApiUrl, Class<T> type) throws IOException {
        return to(tailApiUrl,type,"POST");
    }

    public <T> T to(String tailApiUrl, Class<T> type, String method) throws IOException {
        while (true) {// loop while API rate limit is hit
            HttpURLConnection uc = (HttpURLConnection) root.getApiURL(tailApiUrl).openConnection();

            uc.setDoOutput(true);
            uc.setRequestProperty("Content-type","application/x-www-form-urlencoded");
            if (authenticate) {
                if (root.oauthAccessToken!=null) {
                    uc.setRequestProperty("Authorization", "token " + root.oauthAccessToken);
                } else {
                    if (root.password==null)
                        throw new IllegalArgumentException("V3 API doesn't support API token");
                    uc.setRequestProperty("Authorization", "Basic " + root.encodedAuthorization);
                }
            }
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


            Map json = new HashMap();
            for (Entry e : args) {
                json.put(e.key, e.value);
            }
            MAPPER.writeValue(uc.getOutputStream(),json);

            try {
                InputStreamReader r = new InputStreamReader(uc.getInputStream(), "UTF-8");
                String data = IOUtils.toString(r);
                if (type==null) {
                    return null;
                }
                return MAPPER.readValue(data,type);
            } catch (IOException e) {
                root.handleApiError(e,uc);
            }
        }
    }
}
