package org.kohsuke.github.connector;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import static java.net.HttpURLConnection.HTTP_OK;

/**
 * Response information supplied when a response is received and before the body is processed.
 * <p>
 * During a request to GitHub, {@link GitHubConnector#send(GitHubConnectorRequest)} returns a
 * {@link GitHubConnectorResponse}. This is processed to create a GitHubResponse.
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
    private boolean bodyStreamCalled = false;
    private InputStream bodyStream = null;
    private byte[] bodyBytes = null;
    private boolean isClosed = false;
    private boolean isBodyStreamRereadable;

    /**
     * GitHubConnectorResponse constructor
     *
     * @param request
     *            the request
     * @param statusCode
     *            the status code
     * @param headers
     *            the headers
     */
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
        this.isBodyStreamRereadable = false;
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
     * When {@link #isBodyStreamRereadable} is false, {@link #bodyStream()} can only be called once and the returned
     * stream should be assumed to be read-once and not resetable. This is the default behavior for HTTP_OK responses
     * and significantly reduces memory usage.
     *
     * When {@link #isBodyStreamRereadable} is true, {@link #bodyStream()} can be called be called multiple times. The
     * full stream data is read into a byte array during the first call. Each call returns a new stream backed by the
     * same byte array. This uses more memory, but is required to enable rereading the body stream during trace logging,
     * debugging, and error responses.
     *
     * @return the response body
     * @throws IOException
     *             if response stream is null or an I/O Exception occurs.
     */
    @Nonnull
    public InputStream bodyStream() throws IOException {
        synchronized (this) {
            if (isClosed) {
                throw new IOException("Response is closed");
            }

            if (bodyStreamCalled) {
                if (!isBodyStreamRereadable()) {
                    throw new IOException("Response body not rereadable");
                }
            } else {
                bodyStream = wrapStream(rawBodyStream());
                bodyStreamCalled = true;
            }

            if (bodyStream == null) {
                throw new IOException("Response body missing, stream null");
            } else if (!isBodyStreamRereadable()) {
                return bodyStream;
            }

            // Load rereadable byte array
            if (bodyBytes == null) {
                bodyBytes = IOUtils.toByteArray(bodyStream);
                // Close the raw body stream after successfully reading
                IOUtils.closeQuietly(bodyStream);
            }

            return new ByteArrayInputStream(bodyBytes);
        }
    }

    /**
     * Get the raw implementation specific body stream for this response.
     *
     * This method will only be called once to completion. If an exception is thrown by this method, it may be called
     * multiple times.
     *
     * The stream returned from this method will be closed when the response is closed or sooner. Inheriting classes do
     * not need to close it.
     *
     * @return the stream for the raw response
     * @throws IOException
     *             if an I/O Exception occurs.
     */
    @CheckForNull
    protected abstract InputStream rawBodyStream() throws IOException;

    /**
     * Gets the {@link GitHubConnector} for this response.
     *
     * @return the {@link GitHubConnector} for this response.
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
     * The body stream rereadable state.
     *
     * Body stream defaults to read once for HTTP_OK responses (to reduce memory usage). For non-HTTP_OK responses, body
     * stream is switched to rereadable (in-memory byte array) for error processing.
     *
     * Calling {@link #setBodyStreamRereadable()} will force {@link #isBodyStreamRereadable} to be true for this
     * response regardless of {@link #statusCode} value.
     *
     * @return true when body stream is rereadable.
     */
    public boolean isBodyStreamRereadable() {
        synchronized (this) {
            return isBodyStreamRereadable || statusCode != HTTP_OK;
        }
    }

    /**
     * Force body stream to rereadable regardless of status code.
     *
     * Calling {@link #setBodyStreamRereadable()} will force {@link #isBodyStreamRereadable} to be true for this
     * response regardless of {@link #statusCode} value.
     *
     * This is required to support body value logging during low-level tracing but should be avoided in general since it
     * consumes significantly more memory.
     *
     * Will throw runtime exception if a non-rereadable body stream has already been returned from
     * {@link #bodyStream()}.
     */
    public void setBodyStreamRereadable() {
        synchronized (this) {
            if (bodyStreamCalled && !isBodyStreamRereadable()) {
                throw new RuntimeException("bodyStream() already called in read-once mode");
            }
            isBodyStreamRereadable = true;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        synchronized (this) {
            IOUtils.closeQuietly(bodyStream);
            isClosed = true;
            this.bodyBytes = null;
        }
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

    /**
     * A ByteArrayResponse class
     *
     * @deprecated Inherit directly from {@link GitHubConnectorResponse}.
     */
    @Deprecated
    public abstract static class ByteArrayResponse extends GitHubConnectorResponse {

        /**
         * Constructor for ByteArray Response
         *
         * @param request
         *            the request
         * @param statusCode
         *            the status code
         * @param headers
         *            the headers
         */
        protected ByteArrayResponse(@Nonnull GitHubConnectorRequest request,
                int statusCode,
                @Nonnull Map<String, List<String>> headers) {
            super(request, statusCode, headers);
        }
    }
}
