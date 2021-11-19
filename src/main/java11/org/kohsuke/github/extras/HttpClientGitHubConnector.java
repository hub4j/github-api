package org.kohsuke.github.extras;

import org.kohsuke.github.connector.GitHubConnector;
import org.kohsuke.github.connector.GitHubConnectorRequest;
import org.kohsuke.github.connector.GitHubConnectorResponse;

import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * {@link GitHubConnector} wrapper that sets timeout
 *
 * @author Liam Newman
 */
public class HttpClientGitHubConnector implements GitHubConnector {

    private final HttpClient client;

    /**
     * Instantiates a new HttpClientGitHubConnector.
     */
    public HttpClientGitHubConnector() {
        this(HttpClient.newHttpClient());
    }

    /**
     * Instantiates a new HttpClientGitHubConnector.
     *
     * @param client
     *            the base
     */
    public HttpClientGitHubConnector(HttpClient client) {
        this.client = client;
    }

    @Override
    public GitHubConnectorResponse send(GitHubConnectorRequest connectorRequest) throws IOException {
        HttpRequest.Builder builder = HttpRequest.newBuilder();
        try {
            builder.uri(connectorRequest.url().toURI());
        } catch (URISyntaxException e) {
            throw new IOException("Invalid URL", e);
        }

        for (Map.Entry<String, List<String>> e : connectorRequest.allHeaders().entrySet()) {
            List<String> v = e.getValue();
            if (v != null) {
                builder.header(e.getKey(), String.join(", ", v));
            }
        }

        HttpRequest.BodyPublisher publisher = HttpRequest.BodyPublishers.noBody();
        if (connectorRequest.hasBody()) {
            publisher = HttpRequest.BodyPublishers.ofByteArray(IOUtils.toByteArray(connectorRequest.body()));
        }
        builder.method(connectorRequest.method(), publisher);

        HttpRequest request = builder.build();

        try {
            HttpResponse<InputStream> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            return new HttpClientGitHubConnectorResponse(connectorRequest, httpResponse);
        } catch (InterruptedException e) {
            throw (InterruptedIOException)new InterruptedIOException(e.getMessage()).initCause(e);
        }
    }

    /**
     * Initial response information when a response is initially received and before the body is processed.
     *
     * Implementation specific to {@link HttpResponse}.
     */
    private static class HttpClientGitHubConnectorResponse extends GitHubConnectorResponse {
        private boolean bodyBytesRead = false;
        private byte[] bodyBytes = null;

        @Nonnull
        private final HttpResponse<InputStream> response;

        protected HttpClientGitHubConnectorResponse(@Nonnull GitHubConnectorRequest request, @Nonnull HttpResponse<InputStream> response) {
            super(request, response.statusCode(), response.headers().map());
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
                    try (InputStream stream = wrapStream(response.body())) {
                        if (stream != null) {
                            bodyBytes = IOUtils.toByteArray(stream);
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
         *
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
             IOUtils.closeQuietly(response.body());
        }
    }
}
