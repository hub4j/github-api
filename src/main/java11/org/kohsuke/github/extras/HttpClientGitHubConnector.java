package org.kohsuke.github.extras;

import org.apache.commons.io.IOUtils;
import org.kohsuke.github.connector.GitHubConnector;
import org.kohsuke.github.connector.GitHubConnectorRequest;
import org.kohsuke.github.connector.GitHubConnectorResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * {@link GitHubConnector} for {@link HttpClient}.
 *
 * @author Liam Newman
 * @author Guillaume Smet
 */
public class HttpClientGitHubConnector implements GitHubConnector {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final int MAX_REDIRECTS = 5;

    private final HttpClient client;

    /**
     * Instantiates a new HttpClientGitHubConnector with a default HttpClient.
     */
    public HttpClientGitHubConnector() {
        // We handle redirects manually as Java is copying all the headers when redirected
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
            HttpResponse<InputStream> httpResponse = send(request, 0);
            return new HttpClientGitHubConnectorResponse(connectorRequest, httpResponse);
        } catch (InterruptedException e) {
            throw (InterruptedIOException) new InterruptedIOException(e.getMessage()).initCause(e);
        }
    }

    private HttpResponse<InputStream> send(HttpRequest request, int redirects)
            throws IOException, InterruptedException {
        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (!isRedirecting(response.statusCode()) || redirects > MAX_REDIRECTS) {
            return response;
        }

        URI redirectedUri = getRedirectedUri(request.uri(), response.headers());
        boolean sameHost = redirectedUri.getHost().equalsIgnoreCase(request.uri().getHost());

        // mimicking the behavior of Redirect#NORMAL which was the behavior we used before
        if (!request.uri().getScheme().equalsIgnoreCase(redirectedUri.getScheme())
                && !"https".equalsIgnoreCase(redirectedUri.getScheme())) {
            return response;
        }

        String redirectedMethod = getRedirectedMethod(response.statusCode(), request.method());

        // let's build the new redirected request
        HttpRequest.Builder newRequestBuilder = HttpRequest.newBuilder();
        newRequestBuilder.uri(redirectedUri);
        newRequestBuilder.method(redirectedMethod,
                getRedirectedPublisher(request, response.statusCode(), redirectedMethod));
        for (Entry<String, List<String>> headerEntry : request.headers().map().entrySet()) {
            // if we redirect to a different host, we don't copy the Authorization header
            if (!sameHost && headerEntry.getKey().equalsIgnoreCase(AUTHORIZATION_HEADER)) {
                continue;
            }
            for (String value : headerEntry.getValue()) {
                newRequestBuilder.header(headerEntry.getKey(), value);
            }
        }

        return send(newRequestBuilder.build(), redirects + 1);
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

    private static URI getRedirectedUri(URI originalUri, HttpHeaders headers) throws IOException {
        URI redirectedURI;
        redirectedURI = headers.firstValue("Location")
                .map(URI::create)
                .orElseThrow(() -> new IOException("Invalid redirection"));

        // redirect could be relative to original URL, but if not
        // then redirect is used.
        redirectedURI = originalUri.resolve(redirectedURI);
        return redirectedURI;
    }

    // This implements the exact same rules as the ones applied in RedirectFilter
    private static BodyPublisher getRedirectedPublisher(HttpRequest originalRequest,
            int originalStatusCode,
            String newMethod) {
        if (originalStatusCode == 303 || originalRequest.method().equalsIgnoreCase(newMethod)
                || !originalRequest.bodyPublisher().isPresent()) {
            return HttpRequest.BodyPublishers.noBody();
        }

        return originalRequest.bodyPublisher().get();
    }

    // This implements the exact same rules as the ones applied in RedirectFilter
    private static boolean isRedirecting(int statusCode) {
        return statusCode == 301 || statusCode == 302 || statusCode == 303 || statusCode == 307 || statusCode == 308;
    }

    // This implements the exact same rules as the ones applied in RedirectFilter
    private static String getRedirectedMethod(int statusCode, String originalMethod) {
        switch (statusCode) {
            case 301 :
            case 302 :
                return originalMethod.equals("POST") ? "GET" : originalMethod;
            case 303 :
                return "GET";
            case 307 :
            case 308 :
                return originalMethod;
            default :
                return originalMethod;
        }
    }
}
