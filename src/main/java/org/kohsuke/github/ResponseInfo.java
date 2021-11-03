package org.kohsuke.github;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.apache.commons.io.IOUtils;

/**
 * Initial response information supplied to a {@link GitHubResponse.BodyHandler} when a response is initially received and before
 * the body is processed.
 */
public abstract class ResponseInfo implements Closeable {

    private static final Comparator<String> nullableCaseInsensitiveComparator = Comparator
            .nullsFirst(String.CASE_INSENSITIVE_ORDER);

    private final int statusCode;
    @Nonnull
    private final GitHubRequest request;
    @Nonnull
    private final Map<String, List<String>> headers;

    protected ResponseInfo(@Nonnull GitHubRequest request,
                           int statusCode,
                           @Nonnull Map<String, List<String>> headers) {
        this.request = request;
        this.statusCode = statusCode;

        // Response header field names must be case-insensitive.
        TreeMap<String, List<String>> caseInsensitiveMap = new TreeMap<>(nullableCaseInsensitiveComparator);
        caseInsensitiveMap.putAll(headers);

        this.headers = Collections.unmodifiableMap(caseInsensitiveMap);
    }

    /**
     * Gets the value of a header field for this response.
     *
     * @param name the name of the header field.
     * @return the value of the header field, or {@code null} if the header isn't set.
     */
    @CheckForNull
    public String headerField(String name) {
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
     * @throws IOException if an I/O Exception occurs.
     */
    abstract InputStream bodyStream() throws IOException;

    /**
     * The error message for this response.
     *
     * @return if there is an error with some error string, that is returned. If not, {@code null}.
     */
    abstract String errorMessage();

    /**
     * The {@link URL} for this response.
     *
     * @return the {@link URL} for this response.
     */
    @Nonnull
    public URL url() {
        return request.url();
    }

    /**
     * Gets the {@link GitHubRequest} for this response.
     *
     * @return the {@link GitHubRequest} for this response.
     */
    @Nonnull
    public GitHubRequest request() {
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
    public Map<String, List<String>> headers() {
        return headers;
    }

    /**
     * Gets the body of the response as a {@link String}.
     *
     * @return the body of the response as a {@link String}.
     * @throws IOException if an I/O Exception occurs.
     */
    @Nonnull
    String getBodyAsString() throws IOException {
        InputStreamReader r = new InputStreamReader(this.bodyStream(), StandardCharsets.UTF_8);
        return IOUtils.toString(r);
    }
}
