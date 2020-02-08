package org.kohsuke.github;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

class GitHubResponse<T> {

    private final int statusCode;

    @Nonnull
    private final GitHubRequest request;

    @Nonnull
    private final Map<String, List<String>> headers;

    @CheckForNull
    private final T body;

    GitHubResponse(ResponseInfo responseInfo, T body) {
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

    public T body() {
        return body;
    }

    static class ResponseInfo {

        private final int statusCode;
        @Nonnull
        private final GitHubRequest request;
        @Nonnull
        private final Map<String, List<String>> headers;
        @Nonnull
        final HttpURLConnection connection;

        @Nonnull
        static ResponseInfo fromHttpURLConnection(@Nonnull GitHubRequest request, @Nonnull GitHubClient client)
                throws IOException {
            HttpURLConnection connection;
            try {
                connection = Requester.setupConnection(client, request);
            } catch (IOException e) {
                // An error in here should be wrapped to bypass http exception wrapping.
                throw new GHIOException(e.getMessage(), e);
            }

            // HttpUrlConnection is nuts. This call opens the connection and gets a response.
            // Putting this on it's own line for ease of debugging if needed.
            int statusCode = connection.getResponseCode();
            Map<String, List<String>> headers = connection.getHeaderFields();

            return new ResponseInfo(request, statusCode, headers, connection);
        }

        private ResponseInfo(@Nonnull GitHubRequest request,
                int statusCode,
                @Nonnull Map<String, List<String>> headers,
                @Nonnull HttpURLConnection connection) {
            this.request = request;
            this.statusCode = statusCode;
            this.headers = Collections.unmodifiableMap(new HashMap<>(headers));
            this.connection = connection;
        }

        public String headerField(String name) {
            String result = null;
            if (headers.containsKey(name)) {
                result = headers.get(name).get(0);
            }
            return result;
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

        InputStream wrapInputStream() throws IOException {
            return wrapStream(connection.getInputStream());
        }

        InputStream wrapErrorStream() throws IOException {
            return wrapStream(connection.getErrorStream());
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
    }
}
