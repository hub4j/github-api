/*
 *    Copyright $year slavinson
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.kohsuke.github.extras;

import org.kohsuke.github.HttpConnector;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class PreviewHttpConnector implements HttpConnector {
    private final HttpConnector base;
    private final int readTimeout, connectTimeout;

    /**
     * @param connectTimeout HTTP connection timeout in milliseconds
     * @param readTimeout    HTTP read timeout in milliseconds
     */
    public PreviewHttpConnector(HttpConnector base, int connectTimeout, int readTimeout) {
        this.base = base;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    public PreviewHttpConnector(HttpConnector base, int timeout) {
        this(base, timeout, timeout);
    }

    public PreviewHttpConnector(HttpConnector base) {
        this(base, ImpatientHttpConnector.CONNECT_TIMEOUT, ImpatientHttpConnector.READ_TIMEOUT);
    }

    public PreviewHttpConnector() {
        this(new HttpConnector() {
            public HttpURLConnection connect(URL url) throws IOException {
                return (HttpURLConnection) url.openConnection();
            }
        }, ImpatientHttpConnector.CONNECT_TIMEOUT, ImpatientHttpConnector.READ_TIMEOUT);
    }

    public HttpURLConnection connect(URL url) throws IOException {
        HttpURLConnection con = base.connect(url);
        con.setConnectTimeout(connectTimeout);
        con.setReadTimeout(readTimeout);
        con.addRequestProperty("Accept", PREVIEW_MEDIA_TYPE);
        return con;
    }

    /**
     * Default connection timeout in milliseconds
     */
    public static final String PREVIEW_MEDIA_TYPE = "application/vnd.github.drax-preview+json";
}
