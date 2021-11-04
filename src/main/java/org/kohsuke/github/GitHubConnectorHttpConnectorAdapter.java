package org.kohsuke.github;

import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import javax.annotation.Nonnull;

import static java.util.logging.Level.*;

/**
 * A GitHub API Client for HttpUrlConnection
 * <p>
 * A GitHubConnectorHttpConnectorAdapter can be used to send requests and retrieve their responses. GitHubClient is
 * thread-safe and can be used to send multiple requests. GitHubClient also track some GitHub API information such as
 * {@link GHRateLimit}.
 * </p>
 * <p>
 * GitHubHttpUrlConnectionClient gets a new {@link HttpURLConnection} for each call to send.
 * </p>
 */
class GitHubConnectorHttpConnectorAdapter implements GitHubConnector {

    final HttpConnector httpConnector;

    public GitHubConnectorHttpConnectorAdapter(HttpConnector httpConnector) {
        this.httpConnector = httpConnector;
    }

    @NotNull
    public static GitHubConnector adapt(HttpConnector connector) {
        GitHubConnector gitHubConnector;
        if (connector == HttpConnector.DEFAULT) {
            gitHubConnector = GitHubConnector.DEFAULT;
        } else if (connector == HttpConnector.OFFLINE) {
            gitHubConnector = GitHubConnector.OFFLINE;
        } else if (connector instanceof GitHubConnector) {
            gitHubConnector = (GitHubConnector) connector;
        } else {
            gitHubConnector = new GitHubConnectorHttpConnectorAdapter(connector);
        }
        return gitHubConnector;
    }

    @Override
    public HttpURLConnection connect(URL url) throws IOException {
        return this.httpConnector.connect(url);
    }

    @Nonnull
    public GitHubResponse.ResponseInfo send(GitHubRequest request) throws IOException {
        HttpURLConnection connection;
        try {
            connection = HttpURLConnectionResponseInfo.setupConnection(httpConnector, request);
        } catch (IOException e) {
            // An error in here should be wrapped to bypass http exception wrapping.
            throw new GHIOException(e.getMessage(), e);
        }

        // HttpUrlConnection is nuts. This call opens the connection and gets a response.
        // Putting this on it's own line for ease of debugging if needed.
        int statusCode = connection.getResponseCode();
        Map<String, List<String>> headers = connection.getHeaderFields();

        return new HttpURLConnectionResponseInfo(request, statusCode, headers, connection);
    }

    /**
     * Initial response information supplied to a {@link GitHubResponse.BodyHandler} when a response is initially
     * received and before the body is processed.
     *
     * Implementation specific to {@link HttpURLConnection}.
     */
    static class HttpURLConnectionResponseInfo extends GitHubResponse.ResponseInfo {

        @Nonnull
        private final HttpURLConnection connection;

        HttpURLConnectionResponseInfo(@Nonnull GitHubRequest request,
                int statusCode,
                @Nonnull Map<String, List<String>> headers,
                @Nonnull HttpURLConnection connection) {
            super(request, statusCode, headers);
            this.connection = connection;
        }

        @Nonnull
        static HttpURLConnection setupConnection(@Nonnull HttpConnector connector, @Nonnull GitHubRequest request)
                throws IOException {
            HttpURLConnection connection = connector.connect(request.url());
            setRequestMethod(request.method(), connection);
            buildRequest(request, connection);

            return connection;
        }

        /**
         * Set up the request parameters or POST payload.
         */
        private static void buildRequest(GitHubRequest request, HttpURLConnection connection) throws IOException {
            for (Map.Entry<String, String> e : request.headers().entrySet()) {
                String v = e.getValue();
                if (v != null)
                    connection.setRequestProperty(e.getKey(), v);
            }

            if (request.inBody()) {
                connection.setDoOutput(true);
                try (InputStream body = request.body()) {
                    byte[] bytes = new byte[32768];
                    int read;
                    while ((read = body.read(bytes)) != -1) {
                        connection.getOutputStream().write(bytes, 0, read);
                    }
                }
            }
        }

        private static void setRequestMethod(String method, HttpURLConnection connection) throws IOException {
            try {
                connection.setRequestMethod(method);
            } catch (ProtocolException e) {
                // JDK only allows one of the fixed set of verbs. Try to override that
                try {
                    Field $method = HttpURLConnection.class.getDeclaredField("method");
                    $method.setAccessible(true);
                    $method.set(connection, method);
                } catch (Exception x) {
                    throw (IOException) new IOException("Failed to set the custom verb").initCause(x);
                }
                // sun.net.www.protocol.https.DelegatingHttpsURLConnection delegates to another HttpURLConnection
                try {
                    Field $delegate = connection.getClass().getDeclaredField("delegate");
                    $delegate.setAccessible(true);
                    Object delegate = $delegate.get(connection);
                    if (delegate instanceof HttpURLConnection) {
                        HttpURLConnection nested = (HttpURLConnection) delegate;
                        setRequestMethod(method, nested);
                    }
                } catch (NoSuchFieldException x) {
                    // no problem
                } catch (IllegalAccessException x) {
                    throw (IOException) new IOException("Failed to set the custom verb").initCause(x);
                }
            }
            if (!connection.getRequestMethod().equals(method))
                throw new IllegalStateException("Failed to set the request method to " + method);
        }

        /**
         * {@inheritDoc}
         */
        InputStream bodyStream() throws IOException {
            return wrapStream(connection.getInputStream());
        }

        /**
         * {@inheritDoc}
         */
        String errorMessage() {
            String result = null;
            InputStream stream = null;
            try {
                stream = connection.getErrorStream();
                if (stream != null) {
                    result = IOUtils.toString(wrapStream(stream), StandardCharsets.UTF_8);
                }
            } catch (Exception e) {
                LOGGER.log(FINER, "Ignored exception get error message", e);
            } finally {
                IOUtils.closeQuietly(stream);
            }
            return result;
        }

        /**
         * Handles the "Content-Encoding" header.
         *
         * @param stream
         *            the stream to possibly wrap
         *
         */
        private InputStream wrapStream(InputStream stream) throws IOException {
            String encoding = headerField("Content-Encoding");
            if (encoding == null || stream == null)
                return stream;
            if (encoding.equals("gzip"))
                return new GZIPInputStream(stream);

            throw new UnsupportedOperationException("Unexpected Content-Encoding: " + encoding);
        }

        private static final Logger LOGGER = Logger.getLogger(GitHubClient.class.getName());

        @Override
        public void close() throws IOException {
            IOUtils.closeQuietly(connection.getInputStream());
        }
    }

}
