package org.kohsuke.github;

import org.kohsuke.github.connector.GitHubConnectorResponse;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import javax.annotation.Nonnull;

import static java.net.HttpURLConnection.HTTP_FORBIDDEN;

// TODO: Auto-generated Javadoc
/**
 * Pluggable strategy to determine what to do when the API rate limit is reached.
 *
 * @author Kohsuke Kawaguchi
 * @author Liam Newman
 * @see GitHubBuilder#withAbuseLimitHandler(GitHubAbuseLimitHandler)
 * @see GitHubRateLimitHandler
 */
public abstract class GitHubAbuseLimitHandler extends GitHubConnectorResponseErrorHandler {

    /**
     * On a wait, even if the response suggests a very short wait, wait for a minimum duration.
     */
    private static final int MINIMUM_ABUSE_RETRY_MILLIS = 1000;

    /**
     * Create default GitHubAbuseLimitHandler instance
     */
    public GitHubAbuseLimitHandler() {
    }

    /**
     * Checks if is error.
     *
     * @param connectorResponse
     *            the connector response
     * @return true, if is error
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    boolean isError(@Nonnull GitHubConnectorResponse connectorResponse) {
        return isTooManyRequests(connectorResponse)
                || (isForbidden(connectorResponse) && hasRetryOrLimitHeader(connectorResponse));
    }

    /**
     * Checks if the response status code is TOO_MANY_REQUESTS (429).
     *
     * @param connectorResponse
     *            the response from the GitHub connector
     * @return true if the status code is TOO_MANY_REQUESTS
     */
    private boolean isTooManyRequests(GitHubConnectorResponse connectorResponse) {
        return connectorResponse.statusCode() == TOO_MANY_REQUESTS;
    }

    /**
     * Checks if the response status code is HTTP_FORBIDDEN (403).
     *
     * @param connectorResponse
     *            the response from the GitHub connector
     * @return true if the status code is HTTP_FORBIDDEN
     */
    private boolean isForbidden(GitHubConnectorResponse connectorResponse) {
        return connectorResponse.statusCode() == HTTP_FORBIDDEN;
    }

    /**
     * Checks if the response contains either "Retry-After" or "gh-limited-by" headers. GitHub does not guarantee the
     * presence of the Retry-After header. However, the gh-limited-by header is included in the response when the error
     * is due to rate limiting
     *
     * @param connectorResponse
     *            the response from the GitHub connector
     * @return true if either "Retry-After" or "gh-limited-by" headers are present
     * @see <a href=
     *      "https://docs.github.com/en/rest/using-the-rest-api/best-practices-for-using-the-rest-api?apiVersion=2022-11-28#handle-rate-limit-errors-appropriately">GitHub
     *      API Rate Limiting Documentation</a>
     */
    private boolean hasRetryOrLimitHeader(GitHubConnectorResponse connectorResponse) {
        return hasHeader(connectorResponse, "Retry-After") || hasHeader(connectorResponse, "gh-limited-by");
    }

    /**
     * Checks if the response contains a specific header.
     *
     * @param connectorResponse
     *            the response from the GitHub connector
     * @param headerName
     *            the name of the header to check for
     * @return true if the specified header is present
     */
    private boolean hasHeader(GitHubConnectorResponse connectorResponse, String headerName) {
        return connectorResponse.header(headerName) != null;
    }

    /**
     * Called when the library encounters HTTP error indicating that the API abuse limit is reached.
     *
     * <p>
     * Any exception thrown from this method will cause the request to fail, and the caller of github-api will receive
     * an exception. If this method returns normally, another request will be attempted. For that to make sense, the
     * implementation needs to wait for some time.
     *
     * @param connectorResponse
     *            Response information for this request.
     * @throws IOException
     *             on failure
     * @see <a href="https://developer.github.com/v3/#abuse-rate-limits">API documentation from GitHub</a>
     * @see <a href=
     *      "https://developer.github.com/v3/guides/best-practices-for-integrators/#dealing-with-abuse-rate-limits">Dealing
     *      with abuse rate limits</a>
     *
     */
    public abstract void onError(@Nonnull GitHubConnectorResponse connectorResponse) throws IOException;

    /**
     * Wait until the API abuse "wait time" is passed.
     */
    public static final GitHubAbuseLimitHandler WAIT = new GitHubAbuseLimitHandler() {
        @Override
        public void onError(GitHubConnectorResponse connectorResponse) throws IOException {
            try {
                Thread.sleep(parseWaitTime(connectorResponse));
            } catch (InterruptedException ex) {
                throw (InterruptedIOException) new InterruptedIOException().initCause(ex);
            }
        }
    };

    /**
     * Fail immediately.
     */
    public static final GitHubAbuseLimitHandler FAIL = new GitHubAbuseLimitHandler() {
        @Override
        public void onError(GitHubConnectorResponse connectorResponse) throws IOException {
            throw new HttpException("Abuse limit reached",
                    connectorResponse.statusCode(),
                    connectorResponse.header("Status"),
                    connectorResponse.request().url().toString())
                    .withResponseHeaderFields(connectorResponse.allHeaders());
        }
    };

    // If "Retry-After" missing, wait for unambiguously over one minute per GitHub guidance
    static long DEFAULT_WAIT_MILLIS = Duration.ofSeconds(61).toMillis();

    /*
     * Exposed for testability. Given an http response, find the retry-after header field and parse it as either a
     * number or a date (the spec allows both). If no header is found, wait for a reasonably amount of time.
     */
    static long parseWaitTime(GitHubConnectorResponse connectorResponse) {
        String v = connectorResponse.header("Retry-After");
        if (v == null) {
            return DEFAULT_WAIT_MILLIS;
        }

        try {
            return Math.max(MINIMUM_ABUSE_RETRY_MILLIS, Duration.ofSeconds(Long.parseLong(v)).toMillis());
        } catch (NumberFormatException nfe) {
            // The retry-after header could be a number in seconds, or an http-date
            // We know it was a date if we got a number format exception :)

            // Don't use ZonedDateTime.now(), because the local and remote server times may not be in sync
            // Instead, we can take advantage of the Date field in the response to see what time the remote server
            // thinks it is
            String dateField = connectorResponse.header("Date");
            ZonedDateTime now;
            if (dateField != null) {
                now = ZonedDateTime.parse(dateField, DateTimeFormatter.RFC_1123_DATE_TIME);
            } else {
                now = ZonedDateTime.now();
            }
            ZonedDateTime zdt = ZonedDateTime.parse(v, DateTimeFormatter.RFC_1123_DATE_TIME);
            return Math.max(MINIMUM_ABUSE_RETRY_MILLIS, ChronoUnit.MILLIS.between(now, zdt));
        }
    }

}
