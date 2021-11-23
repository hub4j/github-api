package org.kohsuke.github.connector;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.zip.GZIPInputStream;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Response information supplied when a response is received and before the body is processed.
 * <p>
 * Instances of this class are closed once the response is done being processed. This means that {@link #bodyStream()}
 * will not be readable after a call is completed.
 *
 * {@link #statusCode()}, {@link #allHeaders()}, and {@link #request()} will still be readable but it is recommended
 * that consumers copy any information they need rather than retaining a reference to {@link GitHubConnectorResponse}.
 *
 * @author Liam Newman
 */
public abstract class GitHubConnectorResponse implements Closeable {

    private static final Comparator<String> nullableCaseInsensitiveComparator = Comparator
            .nullsFirst(String.CASE_INSENSITIVE_ORDER);

    private final int statusCode;

    @Nonnull
    private final GitHubConnectorRequest request;
    @Nonnull
    private final Map<String, List<String>> headers;

    protected GitHubConnectorResponse(@Nonnull GitHubConnectorRequest request,
            int statusCode,
            @Nonnull Map<String, List<String>> headers) {
        this.request = request;
        this.statusCode = statusCode;

        // Response header field names must be case-insensitive.
        TreeMap<String, List<String>> caseInsensitiveMap = new TreeMap<>(nullableCaseInsensitiveComparator);
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            caseInsensitiveMap.put(entry.getKey(), Collections.unmodifiableList(new ArrayList<>(entry.getValue())));
        }
        this.headers = Collections.unmodifiableMap(caseInsensitiveMap);
    }

    /**
     * Get this response as a {@link HttpURLConnection}.
     *
     * @return an object that implements at least the response related methods of {@link HttpURLConnection}.
     * @deprecated This method is present only to provide backward compatibility with other deprecated components.
     */
    @Deprecated
    @Nonnull
    public HttpURLConnection toHttpURLConnection() {
        HttpURLConnection connection;
        connection = new GitHubConnectorResponseHttpUrlConnectionAdapter(this);
        return connection;
    }

    /**
     * Gets the value of a header field for this response.
     *
     * @param name
     *            the name of the header field.
     * @return the value of the header field, or {@code null} if the header isn't set.
     */
    @CheckForNull
    public String header(String name) {
        String result = null;
        if (headers.containsKey(name)) {
            result = headers.get(name).get(0);
        }
        return result;
    }

    /**
     * The response body as an {@link InputStream}.
     *
     * @return the response body
     * @throws IOException
     *             if response stream is null or an I/O Exception occurs.
     */
    @Nonnull
    public abstract InputStream bodyStream() throws IOException;

    /**
     * Gets the {@link GitHubConnectorRequest} for this response.
     *
     * @return the {@link GitHubConnectorRequest} for this response.
     */
    @Nonnull
    public GitHubConnectorRequest request() {
        return request;
    }

    /**
     * The status code for this response.
     *
     * @return the status code for this response.
     */
    public int statusCode() {
        return statusCode;
    }

    /**
     * The headers for this response.
     *
     * @return the headers for this response.
     */
    @Nonnull
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Unmodifiable map of unmodifiable lists")
    public Map<String, List<String>> allHeaders() {
        return headers;
    }

    /**
     * Handles wrapping the body stream if indicated by the "Content-Encoding" header.
     *
     * @param stream
     *            the stream to possibly wrap
     * @return an input stream potentially wrapped to decode gzip input
     * @throws IOException
     *             if an I/O Exception occurs.
     */
    protected InputStream wrapStream(InputStream stream) throws IOException {
        String encoding = header("Content-Encoding");
        if (encoding == null || stream == null)
            return stream;
        if (encoding.equals("gzip"))
            return new GZIPInputStream(stream);

        throw new UnsupportedOperationException("Unexpected Content-Encoding: " + encoding);
    }

    /**
     * Parse a header value as a signed decimal integer.
     *
     * @param name
     *            the header field to parse
     * @return integer value of the header field
     * @throws NumberFormatException
     *             if the header is missing or does not contain a parsable integer.
     */
    public final int parseInt(String name) throws NumberFormatException {
        try {
            String headerValue = header(name);
            return Integer.parseInt(headerValue);
        } catch (NumberFormatException e) {
            throw new NumberFormatException(name + ": " + e.getMessage());
        }
    }

    public abstract static class ByteArrayResponse extends GitHubConnectorResponse {

        private boolean inputStreamRead = false;
        private byte[] inputBytes = null;
        private boolean isClosed = false;

        protected ByteArrayResponse(@Nonnull GitHubConnectorRequest request,
                int statusCode,
                @Nonnull Map<String, List<String>> headers) {
            super(request, statusCode, headers);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @Nonnull
        public InputStream bodyStream() throws IOException {
            if (isClosed) {
                throw new IOException("Response is closed");
            }
            synchronized (this) {
                if (!inputStreamRead) {
                    InputStream rawStream = rawBodyStream();
                    try (InputStream stream = wrapStream(rawStream)) {
                        if (stream != null) {
                            inputBytes = IOUtils.toByteArray(stream);
                        }
                    }
                    inputStreamRead = true;
                }
            }

            if (inputBytes == null) {
                throw new IOException("Response body missing, stream null");
            }

            return new ByteArrayInputStream(inputBytes);
        }

        /**
         * Get the raw implementation specific body stream for this response.
         *
         * This method will only be called once to completion. If an exception is thrown, it may be called multiple
         * times.
         *
         * @return the stream for the raw response
         * @throws IOException
         *             if an I/O Exception occurs.
         */
        @CheckForNull
        protected abstract InputStream rawBodyStream() throws IOException;

        @Override
        public void close() throws IOException {
            isClosed = true;
            this.inputBytes = null;
        }
    }
}
