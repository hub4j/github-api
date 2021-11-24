package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.connector.GitHubConnectorRequest;
import org.kohsuke.github.internal.Previews;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.WillClose;

import static java.util.Arrays.asList;

/**
 * Class {@link GitHubRequest} represents an immutable instance used by the client to determine what information to
 * retrieve from a GitHub server. Use the {@link Builder} to construct a {@link GitHubRequest}.
 * <p>
 * NOTE: {@link GitHubRequest} should include the data type to be returned. Any use cases where the same request should
 * be used to return different types of data could be handled in some other way. However, the return type is currently
 * not specified until late in the building process, so this is still untyped.
 * </p>
 *
 * @author Liam Newman
 */
public class GitHubRequest implements GitHubConnectorRequest {

    private static final Comparator<String> nullableCaseInsensitiveComparator = Comparator
            .nullsFirst(String.CASE_INSENSITIVE_ORDER);

    private static final List<String> METHODS_WITHOUT_BODY = asList("GET", "DELETE");
    private final List<Entry> args;
    private final Map<String, List<String>> headers;
    private final Map<String, Object> injectedMappingValues;
    private final String apiUrl;
    private final String urlPath;
    private final String method;
    private final RateLimitTarget rateLimitTarget;
    private final byte[] body;
    private final boolean forceBody;

    private final URL url;

    private GitHubRequest(@Nonnull List<Entry> args,
            @Nonnull Map<String, List<String>> headers,
            @Nonnull Map<String, Object> injectedMappingValues,
            @Nonnull String apiUrl,
            @Nonnull String urlPath,
            @Nonnull String method,
            @Nonnull RateLimitTarget rateLimitTarget,
            @CheckForNull byte[] body,
            boolean forceBody) {
        this.args = Collections.unmodifiableList(new ArrayList<>(args));
        TreeMap<String, List<String>> caseInsensitiveMap = new TreeMap<>(nullableCaseInsensitiveComparator);
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            caseInsensitiveMap.put(entry.getKey(), Collections.unmodifiableList(new ArrayList<>(entry.getValue())));
        }
        this.headers = Collections.unmodifiableMap(caseInsensitiveMap);
        this.injectedMappingValues = Collections.unmodifiableMap(new LinkedHashMap<>(injectedMappingValues));
        this.apiUrl = apiUrl;
        this.urlPath = urlPath;
        this.method = method;
        this.rateLimitTarget = rateLimitTarget;
        this.body = body;
        this.forceBody = forceBody;
        String tailApiUrl = buildTailApiUrl();
        url = getApiURL(apiUrl, tailApiUrl);
    }

    /**
     * Create a new {@link Builder}.
     *
     * @return a new {@link Builder}.
     */
    static Builder<?> newBuilder() {
        return new Builder<>();
    }

    /**
     * Gets the final GitHub API URL.
     *
     * @throws GHException
     *             wrapping a {@link MalformedURLException} if the GitHub API URL cannot be constructed
     */
    @Nonnull
    static URL getApiURL(String apiUrl, String tailApiUrl) {
        try {
            if (!tailApiUrl.startsWith("/")) {
                apiUrl = "";
            } else if ("github.com".equals(apiUrl)) {
                // backward compatibility
                apiUrl = GitHubClient.GITHUB_URL;
            }
            return new URL(apiUrl + tailApiUrl);
        } catch (Exception e) {
            // The data going into constructing this URL should be controlled by the GitHub API framework,
            // so a malformed URL here is a framework runtime error.
            // All callers of this method ended up wrapping and throwing GHException,
            // indicating the functionality should be moved to the common code path.
            throw new GHException("Unable to build GitHub API URL", e);
        }
    }

    /**
     * Transform Java Enum into Github constants given its conventions
     *
     * @param en
     *            Enum to be transformed
     * @return a String containing the value of a Github constant
     */
    static String transformEnum(Enum<?> en) {
        // by convention Java constant names are upper cases, but github uses
        // lower-case constants. GitHub also uses '-', which in Java we always
        // replace with '_'
        return en.toString().toLowerCase(Locale.ENGLISH).replace('_', '-');
    }

    /**
     * The method for this request, such as "GET", "PATCH", or "DELETE".
     *
     * @return the request method.
     */
    @Override
    @Nonnull
    public String method() {
        return method;
    }

    /**
     * The rate limit target for this request.
     *
     * @return the rate limit to use for this request.
     */
    @Nonnull
    public RateLimitTarget rateLimitTarget() {
        return rateLimitTarget;
    }

    /**
     * The arguments for this request. Depending on the {@link #method()} and {@code #inBody()} these maybe added to the
     * url or to the request body.
     *
     * @return the list of arguments
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Already unmodifiable")
    @Nonnull
    public List<Entry> args() {
        return args;
    }

    /**
     * The headers for this request.
     *
     * @return the {@link Map} of headers
     */
    @Override
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Unmodifiable Map of unmodifiable lists")
    @Nonnull
    public Map<String, List<String>> allHeaders() {
        return headers;
    }

    /**
     * Gets the first value of a header field for this request.
     *
     * @param name
     *            the name of the header field.
     * @return the value of the header field, or {@code null} if the header isn't set.
     */
    @CheckForNull
    public String header(String name) {
        List<String> values = headers.get(name);
        if (values != null) {
            return values.get(0);
        }
        return null;
    }

    /**
     * The headers for this request.
     *
     * @return the {@link Map} of headers
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Already unmodifiable")
    @Nonnull
    public Map<String, Object> injectedMappingValues() {
        return injectedMappingValues;
    }

    /**
     * The base GitHub API URL for this request represented as a {@link String}
     *
     * @return the url string
     */
    @Nonnull
    public String apiUrl() {
        return apiUrl;
    }

    /**
     * The url path to be added to the {@link #apiUrl()} for this request. If this does not start with a "/", it instead
     * represents the full url string for this request.
     *
     * @return a url path or full url string
     */
    @Nonnull
    public String urlPath() {
        return urlPath;
    }

    /**
     * The content type to to be sent by this request.
     *
     * @return the content type.
     */
    @Override
    public String contentType() {
        return header("Content-type");
    }

    /**
     * The {@link InputStream} to be sent as the body of this request.
     *
     * @return the {@link InputStream}.
     */
    @Override
    @CheckForNull
    public InputStream body() {
        return body != null ? new ByteArrayInputStream(body) : null;
    }

    /**
     * The {@link URL} for this request. This is the actual URL the {@link GitHubClient} will send this request to.
     *
     * @return the request {@link URL}
     */
    @Override
    @Nonnull
    public URL url() {
        return url;
    }

    /**
     * Whether arguments for this request should be included in the URL or in the body of the request.
     *
     * @return true if the arguements should be sent in the body of the request.
     */
    @Override
    public boolean hasBody() {
        return forceBody || !METHODS_WITHOUT_BODY.contains(method);
    }

    /**
     * Create a {@link Builder} from this request. Initial values of the builder will be the same as this
     * {@link GitHubRequest}.
     *
     * @return a {@link Builder} based on this request.
     */
    Builder<?> toBuilder() {
        return new Builder<>(args,
                headers,
                injectedMappingValues,
                apiUrl,
                urlPath,
                method,
                rateLimitTarget,
                body,
                forceBody);
    }

    private String buildTailApiUrl() {
        String tailApiUrl = urlPath;
        if (!hasBody() && !args.isEmpty() && tailApiUrl.startsWith("/")) {
            try {
                StringBuilder argString = new StringBuilder();
                boolean questionMarkFound = tailApiUrl.indexOf('?') != -1;
                argString.append(questionMarkFound ? '&' : '?');

                for (Iterator<Entry> it = args.listIterator(); it.hasNext();) {
                    Entry arg = it.next();
                    argString.append(URLEncoder.encode(arg.key, StandardCharsets.UTF_8.name()));
                    argString.append('=');
                    argString.append(URLEncoder.encode(arg.value.toString(), StandardCharsets.UTF_8.name()));
                    if (it.hasNext()) {
                        argString.append('&');
                    }
                }
                tailApiUrl += argString;
            } catch (UnsupportedEncodingException e) {
                throw new GHException("UTF-8 encoding required", e);
            }
        }
        return tailApiUrl;
    }

    /**
     * Class {@link Builder} follows the builder pattern for {@link GitHubRequest}.
     *
     * @param <B>
     *            The type of {@link Builder} to return from the various "with*" methods.
     */
    static class Builder<B extends Builder<B>> {

        @Nonnull
        private final List<Entry> args;

        /**
         * The header values for this request.
         */
        @Nonnull
        private final Map<String, List<String>> headers;

        /**
         * Injected local data map
         */
        @Nonnull
        private final Map<String, Object> injectedMappingValues;

        /**
         * The base GitHub API for this request.
         */
        @Nonnull
        private String apiUrl;

        @Nonnull
        private String urlPath;
        /**
         * Request method.
         */
        @Nonnull
        private String method;

        @Nonnull
        private RateLimitTarget rateLimitTarget;

        private byte[] body;
        private boolean forceBody;

        /**
         * Create a new {@link GitHubRequest.Builder}
         */
        protected Builder() {
            this(new ArrayList<>(),
                    new TreeMap<>(nullableCaseInsensitiveComparator),
                    new LinkedHashMap<>(),
                    GitHubClient.GITHUB_URL,
                    "/",
                    "GET",
                    RateLimitTarget.CORE,
                    null,
                    false);
        }

        private Builder(@Nonnull List<Entry> args,
                @Nonnull Map<String, List<String>> headers,
                @Nonnull Map<String, Object> injectedMappingValues,
                @Nonnull String apiUrl,
                @Nonnull String urlPath,
                @Nonnull String method,
                @Nonnull RateLimitTarget rateLimitTarget,
                @CheckForNull byte[] body,
                boolean forceBody) {
            this.args = new ArrayList<>(args);
            TreeMap<String, List<String>> caseInsensitiveMap = new TreeMap<>(nullableCaseInsensitiveComparator);
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                caseInsensitiveMap.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
            this.headers = caseInsensitiveMap;
            this.injectedMappingValues = new LinkedHashMap<>(injectedMappingValues);
            this.apiUrl = apiUrl;
            this.urlPath = urlPath;
            this.method = method;
            this.rateLimitTarget = rateLimitTarget;
            this.body = body;
            this.forceBody = forceBody;
        }

        /**
         * Builds a {@link GitHubRequest} from this builder.
         *
         * @return a {@link GitHubRequest}
         * @throws GHException
         *             wrapping a {@link MalformedURLException} if the GitHub API URL cannot be constructed
         */
        public GitHubRequest build() {
            return new GitHubRequest(args,
                    headers,
                    injectedMappingValues,
                    apiUrl,
                    urlPath,
                    method,
                    rateLimitTarget,
                    body,
                    forceBody);
        }

        /**
         * With header requester.
         *
         * @param url
         *            the url
         * @return the request builder
         */
        public B withApiUrl(String url) {
            this.apiUrl = url;
            return (B) this;
        }

        /**
         * Sets the request HTTP header.
         * <p>
         * If a header of the same name is already set, this method overrides it.
         *
         * @param name
         *            the name
         * @param value
         *            the value
         * @return the request builder
         */
        public B setHeader(String name, String value) {
            List<String> field = new ArrayList<>();
            field.add(value);
            headers.put(name, field);
            return (B) this;
        }

        /**
         * With header requester.
         *
         * @param name
         *            the name
         * @param value
         *            the value
         * @return the request builder
         */
        public B withHeader(String name, String value) {
            List<String> field = headers.get(name);
            if (field == null) {
                setHeader(name, value);
            } else {
                field.add(value);
            }
            return (B) this;
        }

        /**
         * Object to inject into binding.
         *
         * @param value
         *            the value
         * @return the request builder
         */
        public B injectMappingValue(@NonNull Object value) {
            return injectMappingValue(value.getClass().getName(), value);
        }

        /**
         * Object to inject into binding.
         *
         * @param name
         *            the name
         * @param value
         *            the value
         * @return the request builder
         */
        public B injectMappingValue(@NonNull String name, Object value) {
            this.injectedMappingValues.put(name, value);
            return (B) this;
        }

        public B withPreview(String name) {
            return withHeader("Accept", name);
        }

        public B withPreview(Previews preview) {
            return withPreview(preview.mediaType());
        }

        /**
         * With requester.
         *
         * @param map
         *            map of key value pairs to add
         * @return the request builder
         */
        public B with(Map<String, Object> map) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                with(entry.getKey(), entry.getValue());
            }

            return (B) this;
        }

        /**
         * With requester.
         *
         * @param key
         *            the key
         * @param value
         *            the value
         * @return the request builder
         */
        public B with(String key, int value) {
            return with(key, (Object) value);
        }

        /**
         * With requester.
         *
         * @param key
         *            the key
         * @param value
         *            the value
         * @return the request builder
         */
        public B with(String key, long value) {
            return with(key, (Object) value);
        }

        /**
         * With requester.
         *
         * @param key
         *            the key
         * @param value
         *            the value
         * @return the request builder
         */
        public B with(String key, boolean value) {
            return with(key, (Object) value);
        }

        /**
         * With requester.
         *
         * @param key
         *            the key
         * @param e
         *            the e
         * @return the request builder
         */
        public B with(String key, Enum<?> e) {
            if (e == null)
                return with(key, (Object) null);
            return with(key, transformEnum(e));
        }

        /**
         * With requester.
         *
         * @param key
         *            the key
         * @param value
         *            the value
         * @return the request builder
         */
        public B with(String key, String value) {
            return with(key, (Object) value);
        }

        /**
         * With requester.
         *
         * @param key
         *            the key
         * @param value
         *            the value
         * @return the request builder
         */
        public B with(String key, Collection<?> value) {
            return with(key, (Object) value);
        }

        /**
         * With requester.
         *
         * @param key
         *            the key
         * @param value
         *            the value
         * @return the request builder
         */
        public B with(String key, Map<?, ?> value) {
            return with(key, (Object) value);
        }

        /**
         * With requester.
         *
         * @param body
         *            the body
         * @return the request builder
         */
        public B with(@WillClose InputStream body) throws IOException {
            this.body = IOUtils.toByteArray(body);
            IOUtils.closeQuietly(body);
            return (B) this;
        }

        /**
         * With nullable requester.
         *
         * @param key
         *            the key
         * @param value
         *            the value
         * @return the request builder
         */
        public B withNullable(String key, Object value) {
            args.add(new Entry(key, value));
            return (B) this;
        }

        /**
         * With requester.
         *
         * @param key
         *            the key
         * @param value
         *            the value
         * @return the request builder
         */
        public B with(String key, Object value) {
            if (value != null) {
                args.add(new Entry(key, value));
            }
            return (B) this;
        }

        /**
         * Unlike {@link #with(String, String)}, overrides the existing value
         *
         * @param key
         *            the key
         * @param value
         *            the value
         * @return the request builder
         */
        public B set(String key, Object value) {
            remove(key);
            return with(key, value);

        }

        /**
         * Removes all arg entries for a specific key.
         *
         * @param key
         *            the key
         * @return the request builder
         */
        public B remove(String key) {
            for (int index = 0; index < args.size();) {
                if (args.get(index).key.equals(key)) {
                    args.remove(index);
                } else {
                    index++;
                }
            }
            return (B) this;
        }

        /**
         * Method requester.
         *
         * @param method
         *            the method
         * @return the request builder
         */
        public B method(@Nonnull String method) {
            this.method = method;
            return (B) this;
        }

        /**
         * Method requester.
         *
         * @param rateLimitTarget
         *            the rate limit target for this request. Default is {@link RateLimitTarget#CORE}.
         * @return the request builder
         */
        public B rateLimit(@Nonnull RateLimitTarget rateLimitTarget) {
            this.rateLimitTarget = rateLimitTarget;
            return (B) this;
        }

        /**
         * Content type requester.
         *
         * @param contentType
         *            the content type
         * @return the request builder
         */
        public B contentType(String contentType) {
            this.setHeader("Content-type", contentType);
            return (B) this;
        }

        /**
         * NOT FOR PUBLIC USE. Do not make this method public.
         * <p>
         * Sets the path component of api URL without URI encoding.
         * <p>
         * Should only be used when passing a literal URL field from a GHObject, such as {@link GHContent#refresh()} or
         * when needing to set query parameters on requests methods that don't usually have them, such as
         * {@link GHRelease#uploadAsset(String, InputStream, String)}.
         *
         * @param rawUrlPath
         *            the content type
         * @return the request builder
         */
        B setRawUrlPath(@Nonnull String rawUrlPath) {
            Objects.requireNonNull(rawUrlPath);
            // This method should only work for full urls, which must start with "http"
            if (!rawUrlPath.startsWith("http")) {
                throw new GHException("Raw URL must start with 'http'");
            }
            this.urlPath = rawUrlPath;
            return (B) this;
        }

        /**
         * Path component of api URL. Appended to api url.
         * <p>
         * If urlPath starts with a slash, it will be URI encoded as a path. If it starts with anything else, it will be
         * used as is.
         *
         * @param urlPathItems
         *            the content type
         * @return the request builder
         */
        public B withUrlPath(@Nonnull String urlPath, @Nonnull String... urlPathItems) {
            // full url may be set and reset as needed
            if (urlPathItems.length == 0 && !urlPath.startsWith("/")) {
                return setRawUrlPath(urlPath);
            }

            // Once full url is set, do not allow path setting
            if (!this.urlPath.startsWith("/")) {
                throw new GHException("Cannot append to url path after setting a full url");
            }

            String tailUrlPath = urlPath;
            if (urlPathItems.length != 0) {
                tailUrlPath += "/" + String.join("/", urlPathItems);
            }

            tailUrlPath = StringUtils.prependIfMissing(tailUrlPath, "/");

            this.urlPath = urlPathEncode(tailUrlPath);
            return (B) this;
        }

        /**
         * Small number of GitHub APIs use HTTP methods somewhat inconsistently, and use a body where it's not expected.
         * Normally whether parameters go as query parameters or a body depends on the HTTP verb in use, but this method
         * forces the parameters to be sent as a body.
         *
         * @return the request builder
         */
        public B inBody() {
            forceBody = true;
            return (B) this;
        }
    }

    protected static class Entry {
        final String key;
        final Object value;

        protected Entry(String key, Object value) {
            this.key = key;
            this.value = value;
        }
    }

    /**
     * Encode the path to url safe string.
     *
     * @param value
     *            string to be path encoded.
     * @return The encoded string.
     */
    private static String urlPathEncode(String value) {
        try {
            return new URI(null, null, value, null, null).toASCIIString();
        } catch (URISyntaxException ex) {
            throw new AssertionError(ex);
        }
    }

}
