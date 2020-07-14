package org.kohsuke.github;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.function.Consumer;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.net.ssl.SSLHandshakeException;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static java.util.logging.Level.*;

/**
 * A GitHub API Client
 * <p>
 * A GitHubClient can be used to send requests and retrieve their responses. GitHubClient is thread-safe and can be used
 * to send multiple requests. GitHubClient also track some GitHub API information such as {@link #rateLimit()}.
 * </p>
 */
abstract class GitHubClient {

    static final int CONNECTION_ERROR_RETRIES = 2;
    /**
     * If timeout issues let's retry after milliseconds.
     */
    static final int retryTimeoutMillis = 100;
    /* private */ final String login;

    /**
     * Value of the authorization header to be sent with the request.
     */
    /* private */ final String encodedAuthorization;

    // Cache of myself object.
    private final String apiUrl;

    protected final RateLimitHandler rateLimitHandler;
    protected final AbuseLimitHandler abuseLimitHandler;
    private final GitHubRateLimitChecker rateLimitChecker;

    private HttpConnector connector;

    private final Object headerRateLimitLock = new Object();
    private GHRateLimit headerRateLimit = null;
    private volatile GHRateLimit rateLimit = null;

    private static final Logger LOGGER = Logger.getLogger(GitHubClient.class.getName());

    private static final ObjectMapper MAPPER = new ObjectMapper();
    static final String GITHUB_URL = "https://api.github.com";

    private static final String[] TIME_FORMATS = { "yyyy/MM/dd HH:mm:ss ZZZZ", "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.S'Z'" // GitHub App endpoints return a different date format
    };

    static {
        MAPPER.setVisibility(new VisibilityChecker.Std(NONE, NONE, NONE, NONE, ANY));
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true);
        MAPPER.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    }

    GitHubClient(String apiUrl,
            String login,
            String oauthAccessToken,
            String jwtToken,
            String password,
            HttpConnector connector,
            RateLimitHandler rateLimitHandler,
            AbuseLimitHandler abuseLimitHandler,
            GitHubRateLimitChecker rateLimitChecker,
            Consumer<GHMyself> myselfConsumer) throws IOException {

        if (apiUrl.endsWith("/")) {
            apiUrl = apiUrl.substring(0, apiUrl.length() - 1); // normalize
        }

        if (null == connector) {
            connector = HttpConnector.DEFAULT;
        }
        this.apiUrl = apiUrl;
        this.connector = connector;

        if (oauthAccessToken != null) {
            encodedAuthorization = "token " + oauthAccessToken;
        } else {
            if (jwtToken != null) {
                encodedAuthorization = "Bearer " + jwtToken;
            } else if (password != null) {
                String authorization = (login + ':' + password);
                String charsetName = StandardCharsets.UTF_8.name();
                encodedAuthorization = "Basic "
                        + Base64.getEncoder().encodeToString(authorization.getBytes(charsetName));
            } else {// anonymous access
                encodedAuthorization = null;
            }
        }

        this.rateLimitHandler = rateLimitHandler;
        this.abuseLimitHandler = abuseLimitHandler;
        this.rateLimitChecker = rateLimitChecker;

        if (login == null && encodedAuthorization != null && jwtToken == null) {
            GHMyself myself = fetch(GHMyself.class, "/user");
            login = myself.getLogin();
            if (myselfConsumer != null) {
                myselfConsumer.accept(myself);
            }
        }
        this.login = login;
    }

    private <T> T fetch(Class<T> type, String urlPath) throws IOException {
        return this
                .sendRequest(GitHubRequest.newBuilder().withApiUrl(getApiUrl()).withUrlPath(urlPath).build(),
                        (responseInfo) -> GitHubResponse.parseBody(responseInfo, type))
                .body();
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
            if (LOGGER.isLoggable(FINE))
                LOGGER.log(FINE,
                        "Exception validating credentials on " + getApiUrl() + " with login '" + login + "' " + e,
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
        return getConnector() == HttpConnector.OFFLINE;
    }

    /**
     * Gets connector.
     *
     * @return the connector
     */
    public HttpConnector getConnector() {
        return connector;
    }

    /**
     * Sets the custom connector used to make requests to GitHub.
     *
     * @param connector
     *            the connector
     * @deprecated HttpConnector should not be changed.
     */
    @Deprecated
    public void setConnector(HttpConnector connector) {
        LOGGER.warning("Connector should not be changed. Please file an issue describing your use case.");
        this.connector = connector;
    }

    /**
     * Is this an anonymous connection
     *
     * @return {@code true} if operations that require authentication will fail.
     */
    public boolean isAnonymous() {
        return login == null && encodedAuthorization == null;
    }

    /**
     * Gets the current rate limit from the server.
     *
     * For some versions of GitHub Enterprise, the {@code /rate_limit} endpoint returns a {@code 404 Not Found}. In
     * that, if {@link #lastRateLimit()} is not {@code null} and is not expired, it will be returned. Otherwise, a
     * placeholder {@link GHRateLimit} instance with {@link GHRateLimit.UnknownLimitRecord}s will be returned.
     *
     * @return the rate limit
     * @throws IOException
     *             the io exception
     */
    @Nonnull
    public GHRateLimit getRateLimit() throws IOException {
        GHRateLimit result;
        try {
            result = fetch(JsonRateLimit.class, "/rate_limit").resources;
        } catch (FileNotFoundException e) {
            // For some versions of GitHub Enterprise, the rate_limit endpoint returns a 404.
            // However some newer versions of GHE include rate limit header information
            // Use that if available
            result = lastRateLimit();
            if (result == null || result.isExpired()) {
                // return a default rate limit
                result = GHRateLimit.Unknown();
            }
        }

        return rateLimit = result;
    }

    /**
     * Returns the most recently observed rate limit data or {@code null} if either there is no rate limit (for example
     * GitHub Enterprise) or if no requests have been made.
     *
     * @return the most recently observed rate limit data or {@code null}.
     */
    @CheckForNull
    public GHRateLimit lastRateLimit() {
        synchronized (headerRateLimitLock) {
            return headerRateLimit;
        }
    }

    /**
     * Gets the current rate limit while trying not to actually make any remote requests unless absolutely necessary.
     *
     * If {@link #lastRateLimit()} is not {@code null} and is not expired, it will be returned. If the information
     * returned from the last call to {@link #getRateLimit()} is not {@code null} and is not expired, then it will be
     * returned. Otherwise, the result of a call to {@link #getRateLimit()} will be returned.
     *
     * @return the current rate limit data.
     * @throws IOException
     *             if there was an error getting current rate limit data.
     */
    @Nonnull
    public GHRateLimit rateLimit() throws IOException {
        synchronized (headerRateLimitLock) {
            if (headerRateLimit != null && !headerRateLimit.isExpired()) {
                return headerRateLimit;
            }
        }
        GHRateLimit result = this.rateLimit;
        if (result == null || result.isExpired()) {
            result = getRateLimit();
        }
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
            fetch(GHApiInfo.class, "/").check(getApiUrl());
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
     * Builds a {@link GitHubRequest}, sends the {@link GitHubRequest} to the server, and uses the
     * {@link GitHubResponse.BodyHandler} to parse the response info and response body data into an instance of
     * {@link T}.
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
            @CheckForNull GitHubResponse.BodyHandler<T> handler) throws IOException {
        return sendRequest(builder.build(), handler);
    }

    /**
     * Sends the {@link GitHubRequest} to the server, and uses the {@link GitHubResponse.BodyHandler} to parse the
     * response info and response body data into an instance of {@link T}.
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
    public <T> GitHubResponse<T> sendRequest(GitHubRequest request, @CheckForNull GitHubResponse.BodyHandler<T> handler)
            throws IOException {
        int retries = CONNECTION_ERROR_RETRIES;

        do {
            // if we fail to create a connection we do not retry and we do not wrap

            GitHubResponse.ResponseInfo responseInfo = null;
            try {
                try {
                    if (LOGGER.isLoggable(FINE)) {
                        LOGGER.log(FINE,
                                "GitHub API request [" + (login == null ? "anonymous" : login) + "]: "
                                        + request.method() + " " + request.url().toString());
                    }

                    rateLimitChecker.checkRateLimit(this, request);

                    responseInfo = getResponseInfo(request);
                    noteRateLimit(responseInfo);
                    detectOTPRequired(responseInfo);

                    if (isInvalidCached404Response(responseInfo)) {
                        // Setting "Cache-Control" to "no-cache" stops the cache from supplying
                        // "If-Modified-Since" or "If-None-Match" values.
                        // This makes GitHub give us current data (not incorrectly cached data)
                        request = request.toBuilder().withHeader("Cache-Control", "no-cache").build();
                        continue;
                    }
                    if (!(isRateLimitResponse(responseInfo) || isAbuseLimitResponse(responseInfo))) {
                        return createResponse(responseInfo, handler);
                    }
                } catch (IOException e) {
                    // For transient errors, retry
                    if (retryConnectionError(e, request.url(), retries)) {
                        continue;
                    }

                    throw interpretApiError(e, request, responseInfo);
                }

                handleLimitingErrors(responseInfo);
            } finally {
                IOUtils.closeQuietly(responseInfo);
            }

        } while (--retries >= 0);

        throw new GHIOException("Ran out of retries for URL: " + request.url().toString());
    }

    @Nonnull
    protected abstract GitHubResponse.ResponseInfo getResponseInfo(GitHubRequest request) throws IOException;

    protected abstract void handleLimitingErrors(@Nonnull GitHubResponse.ResponseInfo responseInfo) throws IOException;

    @Nonnull
    private static <T> GitHubResponse<T> createResponse(@Nonnull GitHubResponse.ResponseInfo responseInfo,
            @CheckForNull GitHubResponse.BodyHandler<T> handler) throws IOException {
        T body = null;
        if (responseInfo.statusCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
            // special case handling for 304 unmodified, as the content will be ""
        } else if (responseInfo.statusCode() == HttpURLConnection.HTTP_ACCEPTED) {

            // Response code 202 means data is being generated.
            // This happens in specific cases:
            // statistics - See https://developer.github.com/v3/repos/statistics/#a-word-about-caching
            // fork creation - See https://developer.github.com/v3/repos/forks/#create-a-fork

            if (responseInfo.url().toString().endsWith("/forks")) {
                LOGGER.log(INFO, "The fork is being created. Please try again in 5 seconds.");
            } else if (responseInfo.url().toString().endsWith("/statistics")) {
                LOGGER.log(INFO, "The statistics are being generated. Please try again in 5 seconds.");
            } else {
                LOGGER.log(INFO,
                        "Received 202 from " + responseInfo.url().toString() + " . Please try again in 5 seconds.");
            }
            // Maybe throw an exception instead?
        } else if (handler != null) {
            body = handler.apply(responseInfo);
        }
        return new GitHubResponse<>(responseInfo, body);
    }

    /**
     * Handle API error by either throwing it or by returning normally to retry.
     */
    private static IOException interpretApiError(IOException e,
            @Nonnull GitHubRequest request,
            @CheckForNull GitHubResponse.ResponseInfo responseInfo) throws IOException {
        // If we're already throwing a GHIOException, pass through
        if (e instanceof GHIOException) {
            return e;
        }

        int statusCode = -1;
        String message = null;
        Map<String, List<String>> headers = new HashMap<>();
        String errorMessage = null;

        if (responseInfo != null) {
            statusCode = responseInfo.statusCode();
            message = responseInfo.headerField("Status");
            headers = responseInfo.headers();
            errorMessage = responseInfo.errorMessage();
        }

        if (errorMessage != null) {
            if (e instanceof FileNotFoundException) {
                // pass through 404 Not Found to allow the caller to handle it intelligently
                e = new GHFileNotFoundException(e.getMessage() + " " + errorMessage, e)
                        .withResponseHeaderFields(headers);
            } else if (statusCode >= 0) {
                e = new HttpException(errorMessage, statusCode, message, request.url().toString(), e);
            } else {
                e = new GHIOException(errorMessage).withResponseHeaderFields(headers);
            }
        } else if (!(e instanceof FileNotFoundException)) {
            e = new HttpException(statusCode, message, request.url().toString(), e);
        }
        return e;
    }

    protected static boolean isRateLimitResponse(@Nonnull GitHubResponse.ResponseInfo responseInfo) {
        return responseInfo.statusCode() == HttpURLConnection.HTTP_FORBIDDEN
                && "0".equals(responseInfo.headerField("X-RateLimit-Remaining"));
    }

    protected static boolean isAbuseLimitResponse(@Nonnull GitHubResponse.ResponseInfo responseInfo) {
        return responseInfo.statusCode() == HttpURLConnection.HTTP_FORBIDDEN
                && responseInfo.headerField("Retry-After") != null;
    }

    private static boolean retryConnectionError(IOException e, URL url, int retries) throws IOException {
        // There are a range of connection errors where we want to wait a moment and just automatically retry
        boolean connectionError = e instanceof SocketException || e instanceof SocketTimeoutException
                || e instanceof SSLHandshakeException;
        if (connectionError && retries > 0) {
            LOGGER.log(INFO,
                    e.getMessage() + " while connecting to " + url + ". Sleeping " + GitHubClient.retryTimeoutMillis
                            + " milliseconds before retrying... ; will try " + retries + " more time(s)");
            try {
                Thread.sleep(GitHubClient.retryTimeoutMillis);
            } catch (InterruptedException ie) {
                throw (IOException) new InterruptedIOException().initCause(e);
            }
            return true;
        }
        return false;
    }

    private static boolean isInvalidCached404Response(GitHubResponse.ResponseInfo responseInfo) {
        // WORKAROUND FOR ISSUE #669:
        // When the Requester detects a 404 response with an ETag (only happpens when the server's 304
        // is bogus and would cause cache corruption), try the query again with new request header
        // that forces the server to not return 304 and return new data instead.
        //
        // This solution is transparent to users of this library and automatically handles a
        // situation that was cause insidious and hard to debug bad responses in caching
        // scenarios. If GitHub ever fixes their issue and/or begins providing accurate ETags to
        // their 404 responses, this will result in at worst two requests being made for each 404
        // responses. However, only the second request will count against rate limit.
        if (responseInfo.statusCode() == 404 && Objects.equals(responseInfo.request().method(), "GET")
                && responseInfo.headerField("ETag") != null
                && !Objects.equals(responseInfo.request().headers().get("Cache-Control"), "no-cache")) {
            LOGGER.log(FINE,
                    "Encountered GitHub invalid cached 404 from " + responseInfo.url()
                            + ". Retrying with \"Cache-Control\"=\"no-cache\"...");
            return true;
        }
        return false;
    }

    private void noteRateLimit(@Nonnull GitHubResponse.ResponseInfo responseInfo) {
        if (responseInfo.request().urlPath().startsWith("/search")) {
            // the search API uses a different rate limit
            return;
        }

        String limitString = responseInfo.headerField("X-RateLimit-Limit");
        if (StringUtils.isBlank(limitString)) {
            // if we are missing a header, return fast
            return;
        }
        String remainingString = responseInfo.headerField("X-RateLimit-Remaining");
        if (StringUtils.isBlank(remainingString)) {
            // if we are missing a header, return fast
            return;
        }
        String resetString = responseInfo.headerField("X-RateLimit-Reset");
        if (StringUtils.isBlank(resetString)) {
            // if we are missing a header, return fast
            return;
        }

        int limit, remaining;
        long reset;
        try {
            limit = Integer.parseInt(limitString);
        } catch (NumberFormatException e) {
            if (LOGGER.isLoggable(FINEST)) {
                LOGGER.log(FINEST, "Malformed X-RateLimit-Limit header value " + limitString, e);
            }
            return;
        }
        try {

            remaining = Integer.parseInt(remainingString);
        } catch (NumberFormatException e) {
            if (LOGGER.isLoggable(FINEST)) {
                LOGGER.log(FINEST, "Malformed X-RateLimit-Remaining header value " + remainingString, e);
            }
            return;
        }
        try {
            reset = Long.parseLong(resetString);
        } catch (NumberFormatException e) {
            if (LOGGER.isLoggable(FINEST)) {
                LOGGER.log(FINEST, "Malformed X-RateLimit-Reset header value " + resetString, e);
            }
            return;
        }

        GHRateLimit.Record observed = new GHRateLimit.Record(limit, remaining, reset, responseInfo);

        updateCoreRateLimit(observed);
    }

    private static void detectOTPRequired(@Nonnull GitHubResponse.ResponseInfo responseInfo) throws GHIOException {
        // 401 Unauthorized == bad creds or OTP request
        if (responseInfo.statusCode() == HTTP_UNAUTHORIZED) {
            // In the case of a user with 2fa enabled, a header with X-GitHub-OTP
            // will be returned indicating the user needs to respond with an otp
            if (responseInfo.headerField("X-GitHub-OTP") != null) {
                throw new GHOTPRequiredException().withResponseHeaderFields(responseInfo.headers());
            }
        }
    }

    void requireCredential() {
        if (isAnonymous())
            throw new IllegalStateException(
                    "This operation requires a credential but none is given to the GitHub constructor");
    }

    /**
     * Update the Rate Limit with the latest info from response header. Due to multi-threading requests might complete
     * out of order, we want to pick the one with the most recent info from the server. Calls
     * {@link #shouldReplace(GHRateLimit.Record, GHRateLimit.Record)}
     *
     * @param observed
     *            {@link GHRateLimit.Record} constructed from the response header information
     */
    private void updateCoreRateLimit(@Nonnull GHRateLimit.Record observed) {
        synchronized (headerRateLimitLock) {
            if (headerRateLimit == null || shouldReplace(observed, headerRateLimit.getCore())) {
                headerRateLimit = GHRateLimit.fromHeaderRecord(observed);
                LOGGER.log(FINE, "Rate limit now: {0}", headerRateLimit);
            }
        }
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
            return response.statusCode() == HTTP_UNAUTHORIZED && response.headerField("X-GitHub-Media-Type") != null;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Determine if one {@link GHRateLimit.Record} should replace another. Header date is only accurate to the second,
     * so we look at the information in the record itself.
     *
     * {@link GHRateLimit.UnknownLimitRecord}s are always replaced by regular {@link GHRateLimit.Record}s. Regular
     * {@link GHRateLimit.Record}s are never replaced by {@link GHRateLimit.UnknownLimitRecord}s. Candidates with
     * resetEpochSeconds later than current record are more recent. Candidates with the same reset and a lower remaining
     * count are more recent. Candidates with an earlier reset are older.
     *
     * @param candidate
     *            {@link GHRateLimit.Record} constructed from the response header information
     * @param current
     *            the current {@link GHRateLimit.Record} record
     */
    static boolean shouldReplace(@Nonnull GHRateLimit.Record candidate, @Nonnull GHRateLimit.Record current) {
        if (candidate instanceof GHRateLimit.UnknownLimitRecord
                && !(current instanceof GHRateLimit.UnknownLimitRecord)) {
            // Unknown candidate never replaces a regular record
            return false;
        } else if (current instanceof GHRateLimit.UnknownLimitRecord
                && !(candidate instanceof GHRateLimit.UnknownLimitRecord)) {
            // Any real record should replace an unknown Record.
            return true;
        } else {
            // records of the same type compare to each other as normal.
            return current.getResetEpochSeconds() < candidate.getResetEpochSeconds()
                    || (current.getResetEpochSeconds() == candidate.getResetEpochSeconds()
                            && current.getRemaining() > candidate.getRemaining());
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
        for (String f : TIME_FORMATS) {
            try {
                SimpleDateFormat df = new SimpleDateFormat(f);
                df.setTimeZone(TimeZone.getTimeZone("GMT"));
                return df.parse(timestamp);
            } catch (ParseException e) {
                // try next
            }
        }
        throw new IllegalStateException("Unable to parse the timestamp: " + timestamp);
    }

    static String printDate(Date dt) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        return df.format(dt);
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
     * Helper for {@link #getMappingObjectReader(GitHubResponse.ResponseInfo)}
     *
     * @param root
     *            the root GitHub object for this reader
     *
     * @return an {@link ObjectReader} instance that can be further configured.
     */
    @Nonnull
    static ObjectReader getMappingObjectReader(@Nonnull GitHub root) {
        ObjectReader reader = getMappingObjectReader((GitHubResponse.ResponseInfo) null);
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
     *            the {@link GitHubResponse.ResponseInfo} to inject for this reader.
     *
     * @return an {@link ObjectReader} instance that can be further configured.
     */
    @Nonnull
    static ObjectReader getMappingObjectReader(@CheckForNull GitHubResponse.ResponseInfo responseInfo) {
        Map<String, Object> injected = new HashMap<>();

        // Required or many things break
        injected.put(GitHubResponse.ResponseInfo.class.getName(), null);
        injected.put(GitHub.class.getName(), null);

        if (responseInfo != null) {
            injected.put(GitHubResponse.ResponseInfo.class.getName(), responseInfo);
            injected.putAll(responseInfo.request().injectedMappingValues());
        }
        return MAPPER.reader(new InjectableValues.Std(injected));
    }
}
