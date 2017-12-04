/*
 * GitHub API for Java
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.kohsuke.github.extras;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.kohsuke.github.HttpConnector;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * {@link HttpConnector} wrapper that sets timeout
 *
 * @author Kohsuke Kawaguchi
 */
public class ImpatientHttpConnector implements HttpConnector {
    private final HttpConnector base;
    private final int readTimeout, connectTimeout;

    /**
     * @param connectTimeout
     *      HTTP connection timeout in milliseconds
     * @param readTimeout
     *      HTTP read timeout in milliseconds
     */
    public ImpatientHttpConnector(HttpConnector base, int connectTimeout, int readTimeout) {
        this.base = base;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    public ImpatientHttpConnector(HttpConnector base, int timeout) {
        this(base,timeout,timeout);
    }

    public ImpatientHttpConnector(HttpConnector base) {
        this(base,CONNECT_TIMEOUT,READ_TIMEOUT);
    }

    public HttpURLConnection connect(URL url) throws IOException {
        HttpURLConnection con = base.connect(url);
        con.setConnectTimeout(connectTimeout);
        con.setReadTimeout(readTimeout);
        return con;
    }

    /**
     * Default connection timeout in milliseconds
     */
    @SuppressFBWarnings("MS_SHOULD_BE_FINAL")
    public static int CONNECT_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(10);

    /**
     * Default read timeout in milliseconds
     */
    @SuppressFBWarnings("MS_SHOULD_BE_FINAL")
    public static int READ_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(10);
}
