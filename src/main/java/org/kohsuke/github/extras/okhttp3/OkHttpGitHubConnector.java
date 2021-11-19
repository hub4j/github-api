package org.kohsuke.github.extras.okhttp3;

import okhttp3.*;
import org.apache.commons.io.IOUtils;
import org.kohsuke.github.*;
import org.kohsuke.github.connector.GitHubConnector;
import org.kohsuke.github.connector.GitHubConnectorRequest;
import org.kohsuke.github.connector.GitHubConnectorResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import javax.annotation.Nonnull;

/**
 * {@link HttpConnector} for {@link OkHttpClient}.
 * <p>
 * Unlike {@link #DEFAULT}, OkHttp does response caching. Making a conditional request against GitHubAPI and receiving a
 * 304 response does not count against the rate limit. See http://developer.github.com/v3/#conditional-requests
 *
 * @author Liam Newman
 * @author Kohsuke Kawaguchi
 */
public class OkHttpGitHubConnector implements GitHubConnector {
    private static final String HEADER_NAME = "Cache-Control";
    private final String maxAgeHeaderValue;

    private final OkHttpClient client;

    /**
     * Instantiates a new Ok http connector.
     *
     * @param client
     *            the client
     */
    public OkHttpGitHubConnector(OkHttpClient client) {
        this(client, 0);
    }

    /**
     * Instantiates a new Ok http connector.
     *
     * @param client
     *            the client
     * @param cacheMaxAge
     *            the cache max age
     */
    public OkHttpGitHubConnector(OkHttpClient client, int cacheMaxAge) {

        OkHttpClient.Builder builder = client.newBuilder();

        builder.connectionSpecs(TlsConnectionSpecs());
        this.client = builder.build();
        if (cacheMaxAge >= 0 && this.client != null && this.client.cache() != null) {
            maxAgeHeaderValue = new CacheControl.Builder().maxAge(cacheMaxAge, TimeUnit.SECONDS).build().toString();
        } else {
            maxAgeHeaderValue = null;
        }
    }

    @Override
    public GitHubConnectorResponse send(GitHubConnectorRequest request) throws IOException {
        Request.Builder builder = new Request.Builder().url(request.url());
        if (maxAgeHeaderValue != null && request.header(HEADER_NAME) == null) {
            // By default OkHttp honors max-age, meaning it will use local cache
            // without checking the network within that timeframe.
            // However, that can result in stale data being returned during that time so
            // we force network-based checking no matter how often the query is made.
            // OkHttp still automatically does ETag checking and returns cached data when
            // GitHub reports 304, but those do not count against rate limit.
            builder.header(HEADER_NAME, maxAgeHeaderValue);
        }

        for (Map.Entry<String, List<String>> e : request.allHeaders().entrySet()) {
            List<String> v = e.getValue();
            if (v != null) {
                builder.addHeader(e.getKey(), String.join(", ", v));
            }
        }

        RequestBody body = null;
        if (request.hasBody()) {
            body = RequestBody.create(IOUtils.toByteArray(request.body()));
        }
        builder.method(request.method(), body);
        Request okhttpRequest = builder.build();
        Response okhttpResponse = client.newCall(okhttpRequest).execute();

        return new OkHttpGitHubConnectorResponse(request, okhttpResponse);
    }

    /** Returns connection spec with TLS v1.2 in it */
    private List<ConnectionSpec> TlsConnectionSpecs() {
        return Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.CLEARTEXT);
    }

    /**
     * Initial response information when a response is initially received and before the body is processed.
     *
     * Implementation specific to {@link okhttp3.Response}.
     */
    private static class OkHttpGitHubConnectorResponse extends GitHubConnectorResponse {

        private boolean bodyBytesRead = false;
        private byte[] bodyBytes = null;

        @Nonnull
        private final Response response;

        OkHttpGitHubConnectorResponse(@Nonnull GitHubConnectorRequest request, @Nonnull Response response) {
            super(request, response.code(), response.headers().toMultimap());
            this.response = response;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public InputStream bodyStream() throws IOException {
            readBodyBytes();
            InputStream stream = bodyBytes == null ? null : new ByteArrayInputStream(bodyBytes);
            return stream;
        }

        private void readBodyBytes() throws IOException {
            synchronized (this) {
                if (!bodyBytesRead) {
                    try (ResponseBody body = response.body()) {
                        if (body != null) {
                            try (InputStream stream = wrapStream(body.byteStream())) {
                                if (stream != null) {
                                    bodyBytes = IOUtils.toByteArray(stream);
                                }
                            }
                        }
                    }
                    bodyBytesRead = true;
                }
            }
        }

        /**
         * Handles the "Content-Encoding" header.
         *
         * @param stream
         *            the stream to possibly wrap
         */
        private InputStream wrapStream(InputStream stream) throws IOException {
            String encoding = header("Content-Encoding");
            if (encoding == null || stream == null)
                return stream;
            if (encoding.equals("gzip"))
                return new GZIPInputStream(stream);

            throw new UnsupportedOperationException("Unexpected Content-Encoding: " + encoding);
        }

        @Override
        public void close() throws IOException {
            response.close();
        }
    }
}
