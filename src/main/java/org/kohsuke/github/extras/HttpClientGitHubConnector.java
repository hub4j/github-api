package org.kohsuke.github.extras;

import org.apache.commons.io.IOUtils;
import org.kohsuke.github.connector.GitHubConnector;
import org.kohsuke.github.connector.GitHubConnectorRequest;
import org.kohsuke.github.connector.GitHubConnectorResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * {@link GitHubConnector} for {@link HttpClient}.
 *
 * @author Liam Newman
 */
public class HttpClientGitHubConnector implements GitHubConnector {

    private final HttpClient client;

    /**
     * Instantiates a new HttpClientGitHubConnector with a default HttpClient.
     */
    public HttpClientGitHubConnector() {
        // GitHubClient handles redirects manually as Java HttpClient copies all the headers when redirecting
        // even when redirecting to a different host which is problematic as we don't want
        // to push the Authorization header when redirected to a different host.
        // This problem was discovered when upload-artifact@v4 was released as the new
        // service we are redirected to for downloading the artifacts doesn't support
        // having the Authorization header set.
        // The new implementation does not push the Authorization header when redirected
        // to a different host, which is similar to what Okhttp is doing:
        // https://github.com/square/okhttp/blob/f9dfd4e8cc070ca2875a67d8f7ad939d95e7e296/okhttp/src/main/kotlin/okhttp3/internal/http/RetryAndFollowUpInterceptor.kt#L313-L318
        // See also https://github.com/arduino/report-size-deltas/pull/83 for more context
        this(HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build());
    }

    /**
     * Instantiates a new HttpClientGitHubConnector.
     *
     * @param client
     *            the HttpClient to be used
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
            throw (InterruptedIOException) new InterruptedIOException(e.getMessage()).initCause(e);
        }
    }

    /**
     * Initial response information when a response is initially received and before the body is processed.
     *
     * Implementation specific to {@link HttpResponse}.
     */
    private static class HttpClientGitHubConnectorResponse extends GitHubConnectorResponse.ByteArrayResponse {

        @Nonnull
        private final HttpResponse<InputStream> response;

        protected HttpClientGitHubConnectorResponse(@Nonnull GitHubConnectorRequest request,
                @Nonnull HttpResponse<InputStream> response) {
            super(request, response.statusCode(), response.headers().map());
            this.response = response;
        }

        @CheckForNull
        @Override
        protected InputStream rawBodyStream() throws IOException {
            return response.body();
        }

        @Override
        public void close() throws IOException {
            super.close();
            IOUtils.closeQuietly(response.body());
        }
    }
}
