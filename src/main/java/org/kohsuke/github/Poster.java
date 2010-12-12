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
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static org.kohsuke.github.GitHub.*;

/**
 * Handles HTTP POST.
 * @author Kohsuke Kawaguchi
 */
class Poster {
    private final GitHub root;
    private final List<String> args = new ArrayList<String>();
    private boolean authenticate;

    Poster(GitHub root) {
        this.root = root;
    }

    public Poster withCredential() {
        root.requireCredential();
        authenticate = true;
        return this;
    }

    public Poster with(String key, int value) {
        return with(key,String.valueOf(value));
    }

    public Poster with(String key, String value) {
        if (value!=null) {
            try {
                args.add(URLEncoder.encode(key,"UTF-8")+'='+URLEncoder.encode(value,"UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new Error(e); // impossible
            }
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
        HttpURLConnection uc = (HttpURLConnection) root.getApiURL(tailApiUrl).openConnection();

        uc.setDoOutput(true);
        uc.setRequestProperty("Content-type","application/x-www-form-urlencoded");
        if (authenticate)
            uc.setRequestProperty("Authorization", "Basic " + root.encodedAuthorization);
        uc.setRequestMethod(method);


        StringBuilder body = new StringBuilder();
        for (String e : args) {
            if (body.length()>0)    body.append('&');
            body.append(e);
        }

        OutputStreamWriter o = new OutputStreamWriter(uc.getOutputStream(), "UTF-8");
        o.write(body.toString());
        o.close();


        try {
            InputStreamReader r = new InputStreamReader(uc.getInputStream(), "UTF-8");
            if (type==null) {
                String data = IOUtils.toString(r);
                return null;
            }
            return MAPPER.readValue(r,type);
        } catch (IOException e) {
            throw (IOException)new IOException(IOUtils.toString(uc.getErrorStream(),"UTF-8")).initCause(e);
        }
    }
}
