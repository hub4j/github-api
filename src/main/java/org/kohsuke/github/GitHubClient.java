package org.kohsuke.github;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import org.apache.commons.io.IOUtils;
import org.kohsuke.github.authorization.AuthorizationProvider;
import org.kohsuke.github.authorization.UserAuthorizationProvider;
import org.kohsuke.github.connector.GitHubConnector;
import org.kohsuke.github.connector.GitHubConnectorRequest;
import org.kohsuke.github.connector.GitHubConnectorResponse;
import org.kohsuke.github.function.FunctionThrows;

import java.io.*;
import java.net.*;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.net.ssl.SSLHandshakeException;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static java.util.logging.Level.*;
import static org.apache.commons.lang3.StringUtils.defaultString;

/**
 * A GitHub API Client
 * <p>
 * A GitHubClient can be used to send requests and retrieve their responses. Uses {@link GitHubConnector} as a pluggable
 * component to communicate using differing HTTP client libraries.
 * <p>
 * GitHubClient is thread-safe and can be used to send multiple requests simultaneously. GitHubClient also tracks some
 * GitHub API information such as {@link GHRateLimit}.
 * </p>
 *
 * @author Liam Newman
 */
class GitHubClient {

    static final int CONNECTION_ERROR_RETRIES = 2;
    /**
     * If timeout issues let's retry after milliseconds.
     */
    static final int retryTimeoutMillis = 100;

    // Cache of myself object.
    private final String apiUrl;

    private final GitHubRateLimitHandler rateLimitHandler;
    private final GitHubAbuseLimitHandler abuseLimitHandler;
    private final GitHubRateLimitChecker rateLimitChecker;
    private final AuthorizationProvider authorizationProvider;

    private GitHubConnector connector;

    @Nonnull
    private final AtomicReference<GHRateLimit> rateLimit = new AtomicReference<>(GHRateLimit.DEFAULT);

    private static final Logger LOGGER = Logger.getLogger(GitHubClient.class.getName());

    private static final ObjectMapper MAPPER = new ObjectMapper();
    static final String GITHUB_URL = "https://api.github.com";

    private static final DateTimeFormatter DATE_TIME_PARSER_SLASHES = DateTimeFormatter
            .ofPattern("yyyy/MM/dd HH:mm:ss Z");

    static {
        MAPPER.setVisibility(new VisibilityChecker.Std(NONE, NONE, NONE, NONE, ANY));
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true);
        MAPPER.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    }

    GitHubClient(String apiUrl,
            GitHubConnector connector,
            GitHubRateLimitHandler rateLimitHandler,
            GitHubAbuseLimitHandler abuseLimitHandler,
            GitHubRateLimitChecker rateLimitChecker,
            AuthorizationProvider authorizationProvider) throws IOException {

        if (apiUrl.endsWith("/")) {
            apiUrl = apiUrl.substring(0, apiUrl.length() - 1); // normalize
        }

        if (null == connector) {
            connector = GitHubConnector.DEFAULT;
        }
        this.apiUrl = apiUrl;
        this.connector = connector;

        // Prefer credential configuration via provider
        this.authorizationProvider = authorizationProvider;

        this.rateLimitHandler = rateLimitHandler;
        this.abuseLimitHandler = abuseLimitHandler;
        this.rateLimitChecker = rateLimitChecker;
    }

    String getLogin() {
        try {
            if (this.authorizationProvider instanceof UserAuthorizationProvider
                    && this.authorizationProvider.getEncodedAuthorization() != null) {

                UserAuthorizationProvider userAuthorizationProvider = (UserAuthorizationProvider) this.authorizationProvider;

                return userAuthorizationProvider.getLogin();
            }
        } catch (IOException e) {
        }
        return null;
    }

    private <T> T fetch(Class<T> type, String urlPath) throws IOException {
        GitHubRequest request = GitHubRequest.newBuilder().withApiUrl(getApiUrl()).withUrlPath(urlPath).build();
        return sendRequest(request, (connectorResponse) -> GitHubResponse.parseBody(connectorResponse, type)).body();
    }

    /**
     * Ensures that the credential for this client is valid.
     *
     * @return the boolean
     */
    public boolean isCredentialValid() {
        try {
            // If 404, ratelimit returns a default value.
            // This works as credential test because invalid credentials returns 401, not 404
            getRateLimit();
            return true;
        } catch (IOException e) {
            LOGGER.log(FINE,
                    "Exception validating credentials on " + getApiUrl() + " with login '" + getLogin() + "' " + e,
                    e);
            return false;
        }
    }

    /**
     * Is this an always offline "connection".
     *
     * @return {@code true} if this is an always offline "connection".
     */
    public boolean isOffline() {
        return connector == GitHubConnector.OFFLINE;
    }

    /**
     * Gets connector.
     *
     * @return the connector
     */
    @Deprecated
    public HttpConnector getConnector() {
        if (!(connector instanceof HttpConnector)) {
            throw new UnsupportedOperationException("This GitHubConnector does not support HttpConnector.connect().");
        }

        LOGGER.warning(
                "HttpConnector and getConnector() are deprecated. " + "Please file an issue describing your use case.");
        return (HttpConnector) connector;
    }

    /**
     * Sets the custom connector used to make requests to GitHub.
     *
     * @param connector
     *            the connector
     * @deprecated HttpConnector should not be changed.
     */
    @Deprecated
    public void setConnector(GitHubConnector connector) {
        LOGGER.warning("Connector should not be changed. Please file an issue describing your use case.");
        this.connector = connector;
    }

    /**
     * Is this an anonymous connection
     *
     * @return {@code true} if operations that require authentication will fail.
     */
    public boolean isAnonymous() {
        try {
            return getLogin() == null && this.authorizationProvider.getEncodedAuthorization() == null;
        } catch (IOException e) {
            // An exception here means that the provider failed to provide authorization parameters,
            // basically meaning the same as "no auth"
            return false;
        }
    }

    /**
     * Gets the current full rate limit information from the server.
     *
     * For some versions of GitHub Enterprise, the {@code /rate_limit} endpoint returns a {@code 404 Not Found}. In that
     * case, the most recent {@link GHRateLimit} information will be returned, including rate limit information returned
     * in the response header for this request in if was present.
     *
     * For most use cases it would be better to implement a {@link RateLimitChecker} and add it via
     * {@link GitHubBuilder#withRateLimitChecker(RateLimitChecker)}.
     *
     * @return the rate limit
     * @throws IOException
     *             the io exception
     */
    @Nonnull
    public GHRateLimit getRateLimit() throws IOException {
        return getRateLimit(RateLimitTarget.NONE);
    }

    @CheckForNull
    String getEncodedAuthorization() throws IOException {
        return authorizationProvider.getEncodedAuthorization();
    }

    @Nonnull
    GHRateLimit getRateLimit(@Nonnull RateLimitTarget rateLimitTarget) throws IOException {
        GHRateLimit result;
        try {
            GitHubRequest request = GitHubRequest.newBuilder()
                    .rateLimit(RateLimitTarget.NONE)
                    .withApiUrl(getApiUrl())
                    .withUrlPath("/rate_limit")
                    .build();
            result = this
                    .sendRequest(request,
                            (connectorResponse) -> GitHubResponse.parseBody(connectorResponse, JsonRateLimit.class))
                    .body().resources;
        } catch (FileNotFoundException e) {
            // For some versions of GitHub Enterprise, the rate_limit endpoint returns a 404.
            LOGGER.log(FINE, "/rate_limit returned 404 Not Found.");

            // However some newer versions of GHE include rate limit header information
            // If the header info is missing and the endpoint returns 404, fill the rate limit
            // with unknown
            result = GHRateLimit.fromRecord(GHRateLimit.UnknownLimitRecord.current(), rateLimitTarget);
        }
        return updateRateLimit(result);
    }

    /**
     * Returns the most recently observed rate limit data.
     *
     * Generally, instead of calling this you should implement a {@link RateLimitChecker} or call
     *
     * @return the most recently observed rate limit data. This may include expired or
     *         {@link GHRateLimit.UnknownLimitRecord} entries.
     * @deprecated implement a {@link RateLimitChecker} and add it via
     *             {@link GitHubBuilder#withRateLimitChecker(RateLimitChecker)}.
     */
    @Nonnull
    @Deprecated
    GHRateLimit lastRateLimit() {
        return rateLimit.get();
    }

    /**
     * Gets the current rate limit for an endpoint while trying not to actually make any remote requests unless
     * absolutely necessary.
     *
     * If the {@link GHRateLimit.Record} for {@code urlPath} is not expired, it is returned. If the
     * {@link GHRateLimit.Record} for {@code urlPath} is expired, {@link #getRateLimit()} will be called to get the
     * current rate limit.
     *
     * @param rateLimitTarget
     *            the endpoint to get the rate limit for.
     *
     * @return the current rate limit data. {@link GHRateLimit.Record}s in this instance may be expired when returned.
     * @throws IOException
     *             if there was an error getting current rate limit data.
     */
    @Nonnull
    GHRateLimit rateLimit(@Nonnull RateLimitTarget rateLimitTarget) throws IOException {
        GHRateLimit result = rateLimit.get();
        // Most of the time rate limit is not expired, so try to avoid locking.
        if (result.getRecord(rateLimitTarget).isExpired()) {
            // if the rate limit is expired, synchronize to ensure
            // only one call to getRateLimit() is made to refresh it.
            synchronized (this) {
                if (rateLimit.get().getRecord(rateLimitTarget).isExpired()) {
                    getRateLimit(rateLimitTarget);
                }
            }
            result = rateLimit.get();
        }
        return result;
    }

    /**
     * Update the Rate Limit with the latest info from response header.
     *
     * Due to multi-threading, requests might complete out of order. This method calls
     * {@link GHRateLimit#getMergedRateLimit(GHRateLimit)} to ensure the most current records are used.
     *
     * @param observed
     *            {@link GHRateLimit.Record} constructed from the response header information
     */
    private GHRateLimit updateRateLimit(@Nonnull GHRateLimit observed) {
        GHRateLimit result = rateLimit.accumulateAndGet(observed, (current, x) -> current.getMergedRateLimit(x));
        LOGGER.log(FINEST, "Rate limit now: {0}", rateLimit.get());
        return result;
    }

    /**
     * Tests the connection.
     *
     * <p>
     * Verify that the API URL and credentials are valid to access this GitHub.
     *
     * <p>
     * This method returns normally if the endpoint is reachable and verified to be GitHub API URL. Otherwise this
     * method throws {@link IOException} to indicate the problem.
     *
     * @throws IOException
     *             the io exception
     */
    public void checkApiUrlValidity() throws IOException {
        try {
            this.fetch(GHApiInfo.class, "/").check(getApiUrl());
        } catch (IOException e) {
            if (isPrivateModeEnabled()) {
                throw (IOException) new IOException(
                        "GitHub Enterprise server (" + getApiUrl() + ") with private mode enabled").initCause(e);
            }
            throw e;
        }
    }

    public String getApiUrl() {
        return apiUrl;
    }

    /**
     * Builds a {@link GitHubRequest}, sends the {@link GitHubRequest} to the server, and uses the {@link BodyHandler}
     * to parse the response info and response body data into an instance of {@link T}.
     *
     * @param builder
     *            used to build the request that will be sent to the server.
     * @param handler
     *            parse the response info and body data into a instance of {@link T}. If null, no parsing occurs and
     *            {@link GitHubResponse#body()} will return null.
     * @param <T>
     *            the type of the parse body data.
     * @return a {@link GitHubResponse} containing the parsed body data as a {@link T}. Parsed instance may be null.
     * @throws IOException
     *             if an I/O Exception occurs
     */
    @Nonnull
    public <T> GitHubResponse<T> sendRequest(@Nonnull GitHubRequest.Builder<?> builder,
            @CheckForNull BodyHandler<T> handler) throws IOException {
        return sendRequest(builder.build(), handler);
    }

    /**
     * Sends the {@link GitHubRequest} to the server, and uses the {@link BodyHandler} to parse the response info and
     * response body data into an instance of {@link T}.
     *
     * @param request
     *            the request that will be sent to the server.
     * @param handler
     *            parse the response info and body data into a instance of {@link T}. If null, no parsing occurs and
     *            {@link GitHubResponse#body()} will return null.
     * @param <T>
     *            the type of the parse body data.
     * @return a {@link GitHubResponse} containing the parsed body data as a {@link T}. Parsed instance may be null.
     * @throws IOException
     *             if an I/O Exception occurs
     */
    @Nonnull
    public <T> GitHubResponse<T> sendRequest(GitHubRequest request, @CheckForNull BodyHandler<T> handler)
            throws IOException {
        int retries = CONNECTION_ERROR_RETRIES;
        GitHubConnectorRequest connectorRequest = prepareConnectorRequest(request);

        do {
            GitHubConnectorResponse connectorResponse = null;
            try {
                logRequest(connectorRequest);
                rateLimitChecker.checkRateLimit(this, request.rateLimitTarget());
                connectorResponse = connector.send(connectorRequest);
                noteRateLimit(request.rateLimitTarget(), connectorResponse);
                detectKnownErrors(connectorResponse, request, handler != null);
                return createResponse(connectorResponse, handler);
            } catch (RetryRequestException e) {
                // retry requested by requested by error handler (rate limit handler for example)
                if (retries > 0 && e.connectorRequest != null) {
                    connectorRequest = e.connectorRequest;
                }
            } catch (SocketException | SocketTimeoutException | SSLHandshakeException e) {
                // These transient errors the
                if (retries > 0) {
                    logRetryConnectionError(e, request.url(), retries);
                    continue;
                }
                throw interpretApiError(e, connectorRequest, connectorResponse);
            } catch (IOException e) {
                throw interpretApiError(e, connectorRequest, connectorResponse);
            } finally {
                IOUtils.closeQuietly(connectorResponse);
            }
        } while (--retries >= 0);

        throw new GHIOException("Ran out of retries for URL: " + request.url().toString());
    }

    private void detectKnownErrors(GitHubConnectorResponse connectorResponse,
            GitHubRequest request,
            boolean detectStatusCodeError) throws IOException {
        detectOTPRequired(connectorResponse);
        detectInvalidCached404Response(connectorResponse, request);
        if (rateLimitHandler.isError(connectorResponse)) {
            rateLimitHandler.onError(connectorResponse);
            throw new RetryRequestException();
        } else if (abuseLimitHandler.isError(connectorResponse)) {
            abuseLimitHandler.onError(connectorResponse);
            throw new RetryRequestException();
        } else if (detectStatusCodeError
                && GitHubConnectorResponseErrorHandler.STATUS_HTTP_BAD_REQUEST_OR_GREATER.isError(connectorResponse)) {
            GitHubConnectorResponseErrorHandler.STATUS_HTTP_BAD_REQUEST_OR_GREATER.onError(connectorResponse);
        }
    }

    private GitHubConnectorRequest prepareConnectorRequest(GitHubRequest request) throws IOException {
        GitHubRequest.Builder<?> builder = request.toBuilder();
        // if the authentication is needed but no credential is given, try it anyway (so that some calls
        // that do work with anonymous access in the reduced form should still work.)
        if (!request.allHeaders().containsKey("Authorization")) {
            String authorization = getEncodedAuthorization();
            if (authorization != null) {
                builder.setHeader("Authorization", authorization);
            }
        }
        if (request.header("Accept") == null) {
            builder.setHeader("Accept", "application/vnd.github.v3+json");
        }
        builder.setHeader("Accept-Encoding", "gzip");

        if (request.hasBody()) {
            if (request.body() != null) {
                builder.contentType(defaultString(request.contentType(), "application/x-www-form-urlencoded"));
            } else {
                builder.contentType("application/json");
                Map<String, Object> json = new HashMap<>();
                for (GitHubRequest.Entry e : request.args()) {
                    json.put(e.key, e.value);
                }
                builder.with(new ByteArrayInputStream(getMappingObjectWriter().writeValueAsBytes(json)));
            }

        }

        return builder.build();
    }

    private void logRequest(@Nonnull final GitHubConnectorRequest request) {
        LOGGER.log(FINE,
                () -> "GitHub API request [" + (getLogin() == null ? "anonymous" : getLogin()) + "]: "
                        + request.method() + " " + request.url().toString());
    }

    @Nonnull
    private static <T> GitHubResponse<T> createResponse(@Nonnull GitHubConnectorResponse connectorResponse,
            @CheckForNull BodyHandler<T> handler) throws IOException {
        T body = null;
        if (handler != null) {
            if (!shouldIgnoreBody(connectorResponse)) {
                body = handler.apply(connectorResponse);
            }
        }
        return new GitHubResponse<>(connectorResponse, body);
    }

    private static boolean shouldIgnoreBody(@Nonnull GitHubConnectorResponse connectorResponse) {
        if (connectorResponse.statusCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
            // special case handling for 304 unmodified, as the content will be ""
            return true;
        } else if (connectorResponse.statusCode() == HttpURLConnection.HTTP_ACCEPTED) {

            // Response code 202 means data is being generated or an action that can require some time is triggered.
            // This happens in specific cases:
            // statistics - See https://developer.github.com/v3/repos/statistics/#a-word-about-caching
            // fork creation - See https://developer.github.com/v3/repos/forks/#create-a-fork
            // workflow run cancellation - See https://docs.github.com/en/rest/reference/actions#cancel-a-workflow-run

            LOGGER.log(FINE,
                    "Received HTTP_ACCEPTED(202) from " + connectorResponse.request().url().toString()
                            + " . Please try again in 5 seconds.");
            return true;
        } else {
            return false;
        }
    }

    /**
     * Handle API error by either throwing it or by returning normally to retry.
     */
    private static IOException interpretApiError(IOException e,
            @Nonnull GitHubConnectorRequest connectorRequest,
            @CheckForNull GitHubConnectorResponse connectorResponse) throws IOException {
        // If we're already throwing a GHIOException, pass through
        if (e instanceof GHIOException) {
            return e;
        }

        int statusCode = -1;
        String message = null;
        Map<String, List<String>> headers = new HashMap<>();
        String errorMessage = null;

        if (connectorResponse != null) {
            statusCode = connectorResponse.statusCode();
            message = connectorResponse.header("Status");
            headers = connectorResponse.allHeaders();
            if (connectorResponse.statusCode() >= HTTP_BAD_REQUEST) {
                errorMessage = GitHubResponse.getBodyAsStringOrNull(connectorResponse);
            }
        }

        if (errorMessage != null) {
            if (e instanceof FileNotFoundException) {
                // pass through 404 Not Found to allow the caller to handle it intelligently
                e = new GHFileNotFoundException(e.getMessage() + " " + errorMessage, e)
                        .withResponseHeaderFields(headers);
            } else if (statusCode >= 0) {
                e = new HttpException(errorMessage, statusCode, message, connectorRequest.url().toString(), e);
            } else {
                e = new GHIOException(errorMessage).withResponseHeaderFields(headers);
            }
        } else if (!(e instanceof FileNotFoundException)) {
            e = new HttpException(statusCode, message, connectorRequest.url().toString(), e);
        }
        return e;
    }

    private static void logRetryConnectionError(IOException e, URL url, int retries) throws IOException {
        // There are a range of connection errors where we want to wait a moment and just automatically retry
        LOGGER.log(INFO,
                e.getMessage() + " while connecting to " + url + ". Sleeping " + GitHubClient.retryTimeoutMillis
                        + " milliseconds before retrying... ; will try " + retries + " more time(s)");
        try {
            Thread.sleep(GitHubClient.retryTimeoutMillis);
        } catch (InterruptedException ie) {
            throw (IOException) new InterruptedIOException().initCause(e);
        }
    }

    private void detectInvalidCached404Response(GitHubConnectorResponse connectorResponse, GitHubRequest request)
            throws IOException {
        // WORKAROUND FOR ISSUE #669:
        // When the Requester detects a 404 response with an ETag (only happens when the server's 304
        // is bogus and would cause cache corruption), try the query again with new request header
        // that forces the server to not return 304 and return new data instead.
        //
        // This solution is transparent to users of this library and automatically handles a
        // situation that was cause insidious and hard to debug bad responses in caching
        // scenarios. If GitHub ever fixes their issue and/or begins providing accurate ETags to
        // their 404 responses, this will result in at worst two requests being made for each 404
        // responses. However, only the second request will count against rate limit.
        if (connectorResponse.statusCode() == 404 && Objects.equals(connectorResponse.request().method(), "GET")
                && connectorResponse.header("ETag") != null
                && !Objects.equals(connectorResponse.request().header("Cache-Control"), "no-cache")) {
            LOGGER.log(FINE,
                    "Encountered GitHub invalid cached 404 from " + connectorResponse.request().url()
                            + ". Retrying with \"Cache-Control\"=\"no-cache\"...");
            // Setting "Cache-Control" to "no-cache" stops the cache from supplying
            // "If-Modified-Since" or "If-None-Match" values.
            // This makes GitHub give us current data (not incorrectly cached data)
            throw new RetryRequestException(
                    prepareConnectorRequest(request.toBuilder().setHeader("Cache-Control", "no-cache").build()));
        }
    }

    private void noteRateLimit(@Nonnull RateLimitTarget rateLimitTarget,
            @Nonnull GitHubConnectorResponse connectorResponse) {
        try {
            String limitString = Objects.requireNonNull(connectorResponse.header("X-RateLimit-Limit"),
                    "Missing X-RateLimit-Limit");
            String remainingString = Objects.requireNonNull(connectorResponse.header("X-RateLimit-Remaining"),
                    "Missing X-RateLimit-Remaining");
            String resetString = Objects.requireNonNull(connectorResponse.header("X-RateLimit-Reset"),
                    "Missing X-RateLimit-Reset");
            int limit, remaining;
            long reset;
            limit = Integer.parseInt(limitString);
            remaining = Integer.parseInt(remainingString);
            reset = Long.parseLong(resetString);
            GHRateLimit.Record observed = new GHRateLimit.Record(limit, remaining, reset, connectorResponse);
            updateRateLimit(GHRateLimit.fromRecord(observed, rateLimitTarget));
        } catch (NumberFormatException | NullPointerException e) {
            LOGGER.log(FINEST, "Missing or malformed X-RateLimit header: ", e);
        }
    }

    private static void detectOTPRequired(@Nonnull GitHubConnectorResponse connectorResponse) throws GHIOException {
        // 401 Unauthorized == bad creds or OTP request
        if (connectorResponse.statusCode() == HTTP_UNAUTHORIZED) {
            // In the case of a user with 2fa enabled, a header with X-GitHub-OTP
            // will be returned indicating the user needs to respond with an otp
            if (connectorResponse.header("X-GitHub-OTP") != null) {
                throw new GHOTPRequiredException().withResponseHeaderFields(connectorResponse.allHeaders());
            }
        }
    }

    void requireCredential() {
        if (isAnonymous())
            throw new IllegalStateException(
                    "This operation requires a credential but none is given to the GitHub constructor");
    }

    private static class GHApiInfo {
        private String rate_limit_url;

        void check(String apiUrl) throws IOException {
            if (rate_limit_url == null)
                throw new IOException(apiUrl + " doesn't look like GitHub API URL");

            // make sure that the URL is legitimate
            new URL(rate_limit_url);
        }
    }

    /**
     * Checks if a GitHub Enterprise server is configured in private mode.
     *
     * In private mode response looks like:
     *
     * <pre>
     *  $ curl -i https://github.mycompany.com/api/v3/
     *     HTTP/1.1 401 Unauthorized
     *     Server: GitHub.com
     *     Date: Sat, 05 Mar 2016 19:45:01 GMT
     *     Content-Type: application/json; charset=utf-8
     *     Content-Length: 130
     *     Status: 401 Unauthorized
     *     X-GitHub-Media-Type: github.v3
     *     X-XSS-Protection: 1; mode=block
     *     X-Frame-Options: deny
     *     Content-Security-Policy: default-src 'none'
     *     Access-Control-Allow-Credentials: true
     *     Access-Control-Expose-Headers: ETag, Link, X-GitHub-OTP, X-RateLimit-Limit, X-RateLimit-Remaining, X-RateLimit-Reset, X-OAuth-Scopes, X-Accepted-OAuth-Scopes, X-Poll-Interval
     *     Access-Control-Allow-Origin: *
     *     X-GitHub-Request-Id: dbc70361-b11d-4131-9a7f-674b8edd0411
     *     Strict-Transport-Security: max-age=31536000; includeSubdomains; preload
     *     X-Content-Type-Options: nosniff
     * </pre>
     *
     * @return {@code true} if private mode is enabled. If it tries to use this method with GitHub, returns {@code
     * false}.
     */
    private boolean isPrivateModeEnabled() {
        try {
            GitHubResponse<?> response = sendRequest(GitHubRequest.newBuilder().withApiUrl(getApiUrl()), null);
            return response.statusCode() == HTTP_UNAUTHORIZED && response.header("X-GitHub-Media-Type") != null;
        } catch (IOException e) {
            return false;
        }
    }

    static URL parseURL(String s) {
        try {
            return s == null ? null : new URL(s);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Invalid URL: " + s);
        }
    }

    static Date parseDate(String timestamp) {
        if (timestamp == null)
            return null;

        return Date.from(parseInstant(timestamp));
    }

    static Instant parseInstant(String timestamp) {
        if (timestamp == null)
            return null;

        if (timestamp.charAt(4) == '/') {
            // Unsure where this is used, but retained for compatibility.
            return Instant.from(DATE_TIME_PARSER_SLASHES.parse(timestamp));
        } else {
            return Instant.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(timestamp));
        }
    }

    static String printDate(Date dt) {
        return DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(dt.getTime()).truncatedTo(ChronoUnit.SECONDS));
    }

    /**
     * Gets an {@link ObjectWriter}.
     *
     * @return an {@link ObjectWriter} instance that can be further configured.
     */
    @Nonnull
    static ObjectWriter getMappingObjectWriter() {
        return MAPPER.writer();
    }

    /**
     * Helper for {@link #getMappingObjectReader(GitHubConnectorResponse)}
     *
     * @param root
     *            the root GitHub object for this reader
     *
     * @return an {@link ObjectReader} instance that can be further configured.
     */
    @Nonnull
    static ObjectReader getMappingObjectReader(@Nonnull GitHub root) {
        ObjectReader reader = getMappingObjectReader((GitHubConnectorResponse) null);
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
     * @param connectorResponse
     *            the {@link GitHubConnectorResponse} to inject for this reader.
     *
     * @return an {@link ObjectReader} instance that can be further configured.
     */
    @Nonnull
    static ObjectReader getMappingObjectReader(@CheckForNull GitHubConnectorResponse connectorResponse) {
        Map<String, Object> injected = new HashMap<>();

        // Required or many things break
        injected.put(GitHubConnectorResponse.class.getName(), null);
        injected.put(GitHub.class.getName(), null);

        if (connectorResponse != null) {
            injected.put(GitHubConnectorResponse.class.getName(), connectorResponse);
            GitHubConnectorRequest request = connectorResponse.request();
            // This is cheating, but it is an acceptable cheat for now.
            if (request instanceof GitHubRequest) {
                injected.putAll(((GitHubRequest) connectorResponse.request()).injectedMappingValues());
            }
        }
        return MAPPER.reader(new InjectableValues.Std(injected));
    }

    static <K, V> Map<K, V> unmodifiableMapOrNull(Map<? extends K, ? extends V> map) {
        return map == null ? null : Collections.unmodifiableMap(map);
    }

    static <T> List<T> unmodifiableListOrNull(List<? extends T> list) {
        return list == null ? null : Collections.unmodifiableList(list);
    }

    static class RetryRequestException extends IOException {
        final GitHubConnectorRequest connectorRequest;
        RetryRequestException() {
            this(null);
        }

        RetryRequestException(GitHubConnectorRequest connectorRequest) {
            this.connectorRequest = connectorRequest;
        }
    }

    /**
     * Represents a supplier of results that can throw.
     *
     * @param <T>
     *            the type of results supplied by this supplier
     */
    @FunctionalInterface
    interface BodyHandler<T> extends FunctionThrows<GitHubConnectorResponse, T, IOException> {
    }
}
