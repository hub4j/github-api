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


import javax.annotation.WillClose;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A builder pattern interface for making HTTP call and parsing its output.
 *
 * @author Liam Newman
 */
public interface Requester {

    void setHeader(String name, String value);

    Requester withHeader(String name, String value);

    Requester withPreview(String name);

    Requester with(String key, int value);

    Requester with(String key, long value);

    Requester with(String key, Integer value);

    Requester with(String key, boolean value);

    Requester with(String key, Boolean value);

    Requester with(String key, Enum e);

    Requester with(String key, String value);

    Requester with(String key, Collection<?> value);

    Requester with(String key, Map<String, String> value);

    Requester withPermissions(String key, Map<String, GHPermissionType> value);

    Requester with(@WillClose InputStream body);

    Requester withNullable(String key, Object value);


    Requester withLogins(String key, Collection<GHUser> users);


    Requester _with(String key, Object value);

    /**
     * Unlike {@link #with(String, String)}, overrides the existing value
     */
    Requester set(String key, Object value);

    //Requester setNullable(String key, Object value);

    Requester method(String method);

    Requester contentType(String contentType);

    /**
     * Small number of GitHub APIs use HTTP methods somewhat inconsistently, and use a body where it's not expected.
     * Normally whether parameters go as query parameters or a body depends on the HTTP verb in use,
     * but this method forces the parameters to be sent as a body.
     */
    Requester inBody();

    void to(String tailApiUrl) throws IOException;

    /**
     * Sends a request to the specified URL, and parses the response into the given type via databinding.
     *
     * @throws IOException
     *      if the server returns 4xx/5xx responses.
     * @return
     *      {@link Reader} that reads the response.
     */
    <T> T to(String tailApiUrl, Class<T> type) throws IOException;

    /**
     * Like {@link #to(String, Class)} but updates an existing object instead of creating a new instance.
     */
    <T> T to(String tailApiUrl, T existingInstance) throws IOException;

    /**
     * Makes a request and just obtains the HTTP status code.
     * @return a status code
     */
    int asHttpStatusCode(String tailApiUrl) throws IOException;

    InputStream asStream(String tailApiUrl) throws IOException;

    <T> PagedIterable<T> asPagedIterable(String tailApiUrl, Class<T[]> type, Consumer<T> consumer);

    <T> Iterator<T> asIterator(String tailApiUrl, Class<T> type, int pageSize);

    String getResponseHeader(String header);
}
