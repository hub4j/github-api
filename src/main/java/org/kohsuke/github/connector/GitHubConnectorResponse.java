package org.kohsuke.github.connector;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Response information supplied when a response is received and before the body is processed.
 * <p>
 * Instances of this class are closed once the response is done being processed. This means that contents of the body
 * stream will not be readable after a call is completed. Status code, response headers, and request details will still
 * be readable but it is recommended that consumers copy any information they need rather than retaining a reference.
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
     *             if an I/O Exception occurs.
     */
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

}
