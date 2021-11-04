package org.kohsuke.github;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.io.IOUtils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

/**
 * Initial response information supplied to a {@link GitHubResponse.BodyHandler} when a response is initially received
 * and before the body is processed.
 */
public abstract class ResponseInfo implements Closeable {

    private static final Comparator<String> nullableCaseInsensitiveComparator = Comparator
            .nullsFirst(String.CASE_INSENSITIVE_ORDER);

    private final int statusCode;
    @Nonnull
    private final GitHubRequest request;
    @Nonnull
    private final Map<String, List<String>> headers;

    protected ResponseInfo(@Nonnull GitHubRequest request, int statusCode, @Nonnull Map<String, List<String>> headers) {
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
     * @param name
     *            the name of the header field.
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
     * @throws IOException
     *             if an I/O Exception occurs.
     */
    protected abstract InputStream bodyStream() throws IOException;

    /**
     * The error message for this response.
     *
     * @return if there is an error with some error string, that is returned. If not, {@code null}.
     */
    protected abstract String errorMessage();

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
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Collections.unmodifiableMap")
    public Map<String, List<String>> headers() {
        return headers;
    }

    /**
     * Gets the body of the response as a {@link String}.
     *
     * @return the body of the response as a {@link String}.
     * @throws IOException
     *             if an I/O Exception occurs.
     */
    @Nonnull
    String getBodyAsString() throws IOException {
        InputStreamReader r = new InputStreamReader(this.bodyStream(), StandardCharsets.UTF_8);
        return IOUtils.toString(r);
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.setVisibility(new VisibilityChecker.Std(NONE, NONE, NONE, NONE, ANY));
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true);
        MAPPER.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    }

    /**
     * Gets an {@link ObjectWriter}.
     *
     * @return an {@link ObjectWriter} instance that can be further configured.
     */
    @Nonnull
    public static ObjectWriter getMappingObjectWriter() {
        return MAPPER.writer();
    }

    /**
     * Helper for {@link #getMappingObjectReader(ResponseInfo)}
     *
     * @param root
     *            the root GitHub object for this reader
     *
     * @return an {@link ObjectReader} instance that can be further configured.
     */
    @Nonnull
    static ObjectReader getMappingObjectReader(@Nonnull GitHub root) {
        ObjectReader reader = getMappingObjectReader((ResponseInfo) null);
        ((InjectableValues.Std) reader.getInjectableValues()).addValue(GitHub.class, root);
        return reader;
    }

    /**
     * Gets an {@link ObjectReader}.
     *
     * Members of {@link InjectableValues} must be present even if {@code null}, otherwise classes expecting those
     * values will fail to read. This differs from regular JSONProperties which provide defaults instead of failing.
     *
     * Having one spot to create readers and having it take all injectable values is not a great long term solution but
     * it is sufficient for this first cut.
     *
     * @param responseInfo
     *            the {@link ResponseInfo} to inject for this reader.
     *
     * @return an {@link ObjectReader} instance that can be further configured.
     */
    @Nonnull
    static ObjectReader getMappingObjectReader(@CheckForNull ResponseInfo responseInfo) {
        Map<String, Object> injected = new HashMap<>();

        // Required or many things break
        injected.put(ResponseInfo.class.getName(), null);
        injected.put(GitHub.class.getName(), null);

        if (responseInfo != null) {
            injected.put(ResponseInfo.class.getName(), responseInfo);
            injected.putAll(responseInfo.request().injectedMappingValues());
        }
        return MAPPER.reader(new InjectableValues.Std(injected));
    }
}
