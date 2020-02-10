package org.kohsuke.github;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
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
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.net.ssl.SSLHandshakeException;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static java.util.logging.Level.*;
import static org.apache.commons.lang3.StringUtils.defaultString;

class GitHubClient {

    public static final int CONNECTION_ERROR_RETRIES = 2;
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
    private GHMyself myself;
    private final String apiUrl;

    final RateLimitHandler rateLimitHandler;
    final AbuseLimitHandler abuseLimitHandler;

    private HttpConnector connector;

    private final Object headerRateLimitLock = new Object();
    private GHRateLimit headerRateLimit = null;
    private volatile GHRateLimit rateLimit = null;

    static final ObjectMapper MAPPER = new ObjectMapper();
    static final String GITHUB_URL = "https://api.github.com";

    private static final String[] TIME_FORMATS = { "yyyy/MM/dd HH:mm:ss ZZZZ", "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.S'Z'" // GitHub App endpoints return a different date format
    };

    public GitHubClient(GitHub root,
            String apiUrl,
            String login,
            String oauthAccessToken,
            String jwtToken,
            String password,
            HttpConnector connector,
            RateLimitHandler rateLimitHandler,
            AbuseLimitHandler abuseLimitHandler) throws IOException {
        if (apiUrl.endsWith("/"))
            apiUrl = apiUrl.substring(0, apiUrl.length() - 1); // normalize
        this.apiUrl = apiUrl;
        if (null != connector) {
            this.connector = connector;
        } else {
            this.connector = HttpConnector.DEFAULT;
        }

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

        if (login == null && encodedAuthorization != null && jwtToken == null)
            login = getMyself(root).getLogin();
        this.login = login;
    }

    /**
     * Handle API error by either throwing it or by returning normally to retry.
     */
    static IOException interpretApiError(IOException e,
            @Nonnull GitHubRequest request,
            @CheckForNull GitHubResponse.ResponseInfo responseInfo) throws IOException {
        // If we're already throwing a GHIOException, pass through
        if (e instanceof GHIOException) {
            return e;
        }

        int statusCode = -1;
        String message = null;
        Map<String, List<String>> headers = new HashMap<>();
        InputStream es = null;

        if (responseInfo != null) {
            statusCode = responseInfo.statusCode();
            message = responseInfo.headerField("Status");
            headers = responseInfo.headers();
            es = responseInfo.wrapErrorStream();

        }

        if (es != null) {
            try {
                String error = IOUtils.toString(es, StandardCharsets.UTF_8);
                if (e instanceof FileNotFoundException) {
                    // pass through 404 Not Found to allow the caller to handle it intelligently
                    e = new GHFileNotFoundException(error, e).withResponseHeaderFields(headers);
                } else if (statusCode >= 0) {
                    e = new HttpException(error, statusCode, message, request.url().toString(), e);
                } else {
                    e = new GHIOException(error).withResponseHeaderFields(headers);
                }
            } finally {
                IOUtils.closeQuietly(es);
            }
        } else if (!(e instanceof FileNotFoundException)) {
            e = new HttpException(statusCode, message, request.url().toString(), e);
        }
        return e;
    }
    @Nonnull
    static HttpURLConnection setupConnection(@Nonnull GitHubClient client, @Nonnull GitHubRequest request)
            throws IOException {
        if (LOGGER.isLoggable(FINE)) {
            LOGGER.log(FINE,
                    "GitHub API request [" + (client.login == null ? "anonymous" : client.login) + "]: "
                            + request.method() + " " + request.url().toString());
        }
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
                    MAPPER.writeValue(connection.getOutputStream(), json);
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

    @Nonnull
    public <T> GitHubResponse<T> sendRequest(GitHubRequest request, ResponsBodyHandler<T> parser) throws IOException {
        int retries = CONNECTION_ERROR_RETRIES;

        do {
            // if we fail to create a connection we do not retry and we do not wrap

            GitHubResponse.ResponseInfo responseInfo = null;
            try {
                responseInfo = GitHubResponse.ResponseInfo.fromHttpURLConnection(request, this);
                noteRateLimit(responseInfo);
                detectOTPRequired(responseInfo);

                if (isInvalidCached404Response(responseInfo)) {
                    // Setting "Cache-Control" to "no-cache" stops the cache from supplying
                    // "If-Modified-Since" or "If-None-Match" values.
                    // This makes GitHub give us current data (not incorrectly cached data)
                    request = request.builder().withHeader("Cache-Control", "no-cache").build(this);
                    continue;
                }
                if (!(isRateLimitResponse(responseInfo) || isAbuseLimitResponse(responseInfo))) {
                    T body = null;
                    if (parser != null) {
                        body = parser.apply(responseInfo);
                    }
                    return new GitHubResponse<>(responseInfo, body);
                }
            } catch (IOException e) {
                // For transient errors, retry
                if (retryConnectionError(e, request.url(), retries)) {
                    continue;
                }

                throw interpretApiError(e, request, responseInfo);
            }

            handleLimitingErrors(responseInfo);

        } while (--retries >= 0);

        throw new GHIOException("Ran out of retries for URL: " + request.url().toString());
    }

    private static boolean isRateLimitResponse(@Nonnull GitHubResponse.ResponseInfo responseInfo) {
        return responseInfo.statusCode() == HttpURLConnection.HTTP_FORBIDDEN
                && "0".equals(responseInfo.headerField("X-RateLimit-Remaining"));
    }

    private static boolean isAbuseLimitResponse(@Nonnull GitHubResponse.ResponseInfo responseInfo) {
        return responseInfo.statusCode() == HttpURLConnection.HTTP_FORBIDDEN
                && responseInfo.headerField("Retry-After") != null;
    }

    private void handleLimitingErrors(@Nonnull GitHubResponse.ResponseInfo responseInfo) throws IOException {
        if (isRateLimitResponse(responseInfo)) {
            GHIOException e = new HttpException("Rate limit violation",
                    responseInfo.statusCode(),
                    responseInfo.headerField("Status"),
                    responseInfo.url().toString()).withResponseHeaderFields(responseInfo.headers());
            rateLimitHandler.onError(e, responseInfo.connection);
        } else if (isAbuseLimitResponse(responseInfo)) {
            GHIOException e = new HttpException("Abuse limit violation",
                    responseInfo.statusCode(),
                    responseInfo.headerField("Status"),
                    responseInfo.url().toString()).withResponseHeaderFields(responseInfo.headers());
            abuseLimitHandler.onError(e, responseInfo.connection);
        }
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

        GHRateLimit.Record observed = new GHRateLimit.Record(limit, remaining, reset, responseInfo.headerField("Date"));

        updateCoreRateLimit(observed);
    }

    static void detectOTPRequired(@Nonnull GitHubResponse.ResponseInfo responseInfo) throws GHIOException {
        // 401 Unauthorized == bad creds or OTP request
        if (responseInfo.statusCode() == HTTP_UNAUTHORIZED) {
            // In the case of a user with 2fa enabled, a header with X-GitHub-OTP
            // will be returned indicating the user needs to respond with an otp
            if (responseInfo.headerField("X-GitHub-OTP") != null) {
                throw new GHOTPRequiredException().withResponseHeaderFields(responseInfo.headers());
            }
        }
    }

    Requester createRequest() {
        return new Requester(this);
    }

    /**
     * Gets the {@link GHUser} that represents yourself.
     *
     * @return the myself
     * @throws IOException
     *             the io exception
     */
    GHMyself getMyself(GitHub root) throws IOException {
        requireCredential();
        synchronized (this) {
            if (this.myself != null)
                return myself;

            GHMyself u = createRequest().withUrlPath("/user").fetch(GHMyself.class);
            u.root = root;

            this.myself = u;
            return u;
        }
    }

    /**
     * Ensures that the credential is valid.
     *
     * @return the boolean
     */
    public boolean isCredentialValid() {
        try {
            createRequest().withUrlPath("/user").fetch(GHUser.class);
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
     */
    public void setConnector(HttpConnector connector) {
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

    void requireCredential() {
        if (isAnonymous())
            throw new IllegalStateException(
                    "This operation requires a credential but none is given to the GitHub constructor");
    }

    @Nonnull
    URL getApiURL(String tailApiUrl) throws MalformedURLException {
        if (tailApiUrl.startsWith("/")) {
            if ("github.com".equals(apiUrl)) {// backward compatibility
                return new URL(GitHubClient.GITHUB_URL + tailApiUrl);
            } else {
                return new URL(apiUrl + tailApiUrl);
            }
        } else {
            return new URL(tailApiUrl);
        }
    }

    /**
     * Gets the current rate limit.
     *
     * @return the rate limit
     * @throws IOException
     *             the io exception
     */
    public GHRateLimit getRateLimit() throws IOException {
        GHRateLimit rateLimit;
        try {
            rateLimit = createRequest().withUrlPath("/rate_limit").fetch(JsonRateLimit.class).resources;
        } catch (FileNotFoundException e) {
            // GitHub Enterprise doesn't have the rate limit
            // return a default rate limit that
            rateLimit = GHRateLimit.Unknown();
        }

        return this.rateLimit = rateLimit;
    }

    /**
     * Update the Rate Limit with the latest info from response header. Due to multi-threading requests might complete
     * out of order, we want to pick the one with the most recent info from the server.
     *
     * @param observed
     *            {@link GHRateLimit.Record} constructed from the response header information
     */
    void updateCoreRateLimit(@Nonnull GHRateLimit.Record observed) {
        synchronized (headerRateLimitLock) {
            if (headerRateLimit == null || GitHubClient.shouldReplace(observed, headerRateLimit.getCore())) {
                headerRateLimit = GHRateLimit.fromHeaderRecord(observed);
                LOGGER.log(FINE, "Rate limit now: {0}", headerRateLimit);
            }
        }
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
     * @return the current rate limit data.
     * @throws IOException
     *             if we couldn't get the current rate limit data.
     */
    @Nonnull
    public GHRateLimit rateLimit() throws IOException {
        synchronized (headerRateLimitLock) {
            if (headerRateLimit != null && !headerRateLimit.isExpired()) {
                return headerRateLimit;
            }
        }
        GHRateLimit rateLimit = this.rateLimit;
        if (rateLimit == null || rateLimit.isExpired()) {
            rateLimit = getRateLimit();
        }
        return rateLimit;
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
            createRequest().withUrlPath("/").fetch(GHApiInfo.class).check(apiUrl);
        } catch (IOException e) {
            if (isPrivateModeEnabled()) {
                throw (IOException) new IOException(
                        "GitHub Enterprise server (" + apiUrl + ") with private mode enabled").initCause(e);
            }
            throw e;
        }
    }

    public String getApiUrl() {
        return apiUrl;
    }

    /**
     * Represents a supplier of results that can throw.
     *
     * <p>
     * This is a <a href="package-summary.html">functional interface</a> whose functional method is
     * {@link #apply(GitHubResponse.ResponseInfo)}.
     *
     * @param <T>
     *            the type of results supplied by this supplier
     */
    @FunctionalInterface
    interface ResponsBodyHandler<T> {

        /**
         * Gets a result.
         *
         * @return a result
         * @throws IOException
         */
        T apply(GitHubResponse.ResponseInfo input) throws IOException;
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
            HttpURLConnection uc = connector.connect(getApiURL("/"));
            try {
                return uc.getResponseCode() == HTTP_UNAUTHORIZED && uc.getHeaderField("X-GitHub-Media-Type") != null;
            } finally {
                // ensure that the connection opened by getResponseCode gets closed
                try {
                    IOUtils.closeQuietly(uc.getInputStream());
                } catch (IOException ignore) {
                    // ignore
                }
                IOUtils.closeQuietly(uc.getErrorStream());
            }
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Update the Rate Limit with the latest info from response header. Due to multi-threading requests might complete
     * out of order, we want to pick the one with the most recent info from the server. Header date is only accurate to
     * the second, so we look at the information in the record itself.
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

    static {
        MAPPER.setVisibility(new VisibilityChecker.Std(NONE, NONE, NONE, NONE, ANY));
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true);
        MAPPER.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    }

    private static final Logger LOGGER = Logger.getLogger(GitHubClient.class.getName());

}
