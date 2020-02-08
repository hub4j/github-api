package org.kohsuke.github;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import org.apache.commons.io.IOUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static java.util.logging.Level.FINE;

class GitHubClient {

    final String login;

    /**
     * Value of the authorization header to be sent with the request.
     */
    final String encodedAuthorization;

    // Cache of myself object.
    GHMyself myself;
    final String apiUrl;

    final RateLimitHandler rateLimitHandler;
    final AbuseLimitHandler abuseLimitHandler;

    HttpConnector connector;

    final Object headerRateLimitLock = new Object();
    GHRateLimit headerRateLimit = null;
    volatile GHRateLimit rateLimit = null;

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
    boolean isPrivateModeEnabled() {
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
