/*
 * GitHub API for Java
 * Copyright (C) 2009-2017 SonarSource SA
 * mailto:info AT sonarsource DOT com
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

import com.squareup.okhttp.ConnectionSpec;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;

import org.kohsuke.github.HttpConnector;

import java.io.IOException;

import java.net.HttpURLConnection;
import java.net.URL;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

/**
 * {@link HttpConnector} for {@link OkHttpClient}.
 *
 * Unlike {@link #DEFAULT}, OkHttp does response caching.
 * Making a conditional request against GitHubAPI and receiving a 304
 * response does not count against the rate limit.
 * See http://developer.github.com/v3/#conditional-requests
 *
 * @author Roberto Tyley
 * @author Kohsuke Kawaguchi
 */
public class OkHttpConnector implements HttpConnector {
    private final OkUrlFactory urlFactory;

    public OkHttpConnector(OkUrlFactory urlFactory) {
        urlFactory.client().setSslSocketFactory(TlsSocketFactory());
        urlFactory.client().setConnectionSpecs(TlsConnectionSpecs());
        this.urlFactory = urlFactory;
    }

    public HttpURLConnection connect(URL url) throws IOException {
        return urlFactory.open(url);
    }

    /** Returns TLSv1.2 only SSL Socket Factory. */
    private SSLSocketFactory TlsSocketFactory() {
        SSLContext sc;
        try {
            sc = SSLContext.getInstance("TLSv1.2");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        try {
            sc.init(null, null, null);
            return sc.getSocketFactory();
        } catch (KeyManagementException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /** Returns connection spec with TLS v1.2 in it */
    private List<ConnectionSpec> TlsConnectionSpecs() {
        return Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.CLEARTEXT);
    }
}
