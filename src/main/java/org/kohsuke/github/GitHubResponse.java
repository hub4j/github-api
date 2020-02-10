package org.kohsuke.github;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import static org.apache.commons.lang3.StringUtils.defaultString;

class GitHubResponse<T> {

    private final int statusCode;

    @Nonnull
    private final GitHubRequest request;

    @Nonnull
    private final Map<String, List<String>> headers;

    @CheckForNull
    private final T body;

    GitHubResponse(GitHubResponse<T> response, @CheckForNull T body) {
        this.statusCode = response.statusCode();
        this.request = response.request();
        this.headers = response.headers();
        this.body = body;
    }

    GitHubResponse(ResponseInfo responseInfo, @CheckForNull T body) {
        this.statusCode = responseInfo.statusCode();
        this.request = responseInfo.request();
        this.headers = responseInfo.headers();
        this.body = body;
    }

    @Nonnull
    public URL url() {
        return request.url();
    }

    @Nonnull
    public GitHubRequest request() {
        return request;
    }

    public int statusCode() {
        return statusCode;
    }

    @Nonnull
    public Map<String, List<String>> headers() {
        return headers;
    }

    @CheckForNull
    public String headerField(String name) {
        String result = null;
        if (headers.containsKey(name)) {
            result = headers.get(name).get(0);
        }
        return result;
    }

    @CheckForNull
    public T body() {
        return body;
    }

    static abstract class ResponseInfo {

        private final int statusCode;
        @Nonnull
        private final GitHubRequest request;
        @Nonnull
        private final Map<String, List<String>> headers;

        @Nonnull
        static ResponseInfo fromHttpURLConnection(@Nonnull GitHubRequest request, @Nonnull GitHubClient client)
                throws IOException {
            HttpURLConnection connection;
            try {
                connection = HttpURLConnectionResponseInfo.setupConnection(client, request);
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

        protected ResponseInfo(@Nonnull GitHubRequest request,
                int statusCode,
                @Nonnull Map<String, List<String>> headers) {
            this.request = request;
            this.statusCode = statusCode;
            this.headers = Collections.unmodifiableMap(new HashMap<>(headers));
        }

        @CheckForNull
        public String headerField(String name) {
            String result = null;
            if (headers.containsKey(name)) {
                result = headers.get(name).get(0);
            }
            return result;
        }

        abstract InputStream wrapInputStream() throws IOException;

        abstract InputStream wrapErrorStream() throws IOException;

        @Nonnull
        public URL url() {
            return request.url();
        }

        @Nonnull
        public GitHubRequest request() {
            return request;
        }

        public int statusCode() {
            return statusCode;
        }

        @Nonnull
        public Map<String, List<String>> headers() {
            return headers;
        }

        String getBodyAsString() throws IOException {
            InputStreamReader r = null;
            try {
                r = new InputStreamReader(this.wrapInputStream(), StandardCharsets.UTF_8);
                return IOUtils.toString(r);
            } finally {
                IOUtils.closeQuietly(r);
            }

        }
    }

    static class HttpURLConnectionResponseInfo extends ResponseInfo {

        @Nonnull
        final HttpURLConnection connection;

        private HttpURLConnectionResponseInfo(@Nonnull GitHubRequest request,
                int statusCode,
                @Nonnull Map<String, List<String>> headers,
                @Nonnull HttpURLConnection connection) {
            super(request, statusCode, headers);
            this.connection = connection;
        }

        @Nonnull
        static HttpURLConnection setupConnection(@Nonnull GitHubClient client, @Nonnull GitHubRequest request)
                throws IOException {
            HttpURLConnection connection = client.getConnector().connect(request.url());

            // if the authentication is needed but no credential is given, try it anyway (so that some calls
            // that do work with anonymous access in the reduced form should still work.)
            if (client.encodedAuthorization != null)
                connection.setRequestProperty("Authorization", client.encodedAuthorization);

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
            connection.setRequestProperty("Accept-Encoding", "gzip");

            if (request.inBody()) {
                connection.setDoOutput(true);

                try (InputStream body = request.body()) {
                    if (body != null) {
                        connection.setRequestProperty("Content-type",
                                defaultString(request.contentType(), "application/x-www-form-urlencoded"));
                        byte[] bytes = new byte[32768];
                        int read;
                        while ((read = body.read(bytes)) != -1) {
                            connection.getOutputStream().write(bytes, 0, read);
                        }
                    } else {
                        connection.setRequestProperty("Content-type",
                                defaultString(request.contentType(), "application/json"));
                        Map<String, Object> json = new HashMap<>();
                        for (GitHubRequest.Entry e : request.args()) {
                            json.put(e.key, e.value);
                        }
                        GitHubClient.MAPPER.writeValue(connection.getOutputStream(), json);
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

        InputStream wrapInputStream() throws IOException {
            return wrapStream(connection.getInputStream());
        }

        InputStream wrapErrorStream() throws IOException {
            return wrapStream(connection.getErrorStream());
        }

        /**
         * Handles the "Content-Encoding" header.
         *
         * @param in
         *
         */
        private InputStream wrapStream(InputStream in) throws IOException {
            String encoding = headerField("Content-Encoding");
            if (encoding == null || in == null)
                return in;
            if (encoding.equals("gzip"))
                return new GZIPInputStream(in);

            throw new UnsupportedOperationException("Unexpected Content-Encoding: " + encoding);
        }

        private static final Logger LOGGER = Logger.getLogger(GitHubClient.class.getName());

    }
}
