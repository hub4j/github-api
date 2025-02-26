package org.kohsuke.github;

import org.jetbrains.annotations.NotNull;
import org.kohsuke.github.connector.GitHubConnectorResponse;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.annotation.Nonnull;

import static java.net.HttpURLConnection.HTTP_FORBIDDEN;

// TODO: Auto-generated Javadoc
/**
 * Pluggable strategy to determine what to do when the API rate limit is reached.
 *
 * @author Kohsuke Kawaguchi
 * @author Liam Newman
 * @see GitHubBuilder#withRateLimitHandler(GitHubRateLimitHandler)
 * @see GitHubAbuseLimitHandler
 */
public abstract class GitHubRateLimitHandler extends GitHubConnectorResponseErrorHandler {

    /**
     * On a wait, even if the response suggests a very short wait, wait for a minimum duration.
     */
    private static final int MINIMUM_RATE_LIMIT_RETRY_MILLIS = 1000;

    /**
     * Create default GitHubRateLimitHandler instance
     */
    public GitHubRateLimitHandler() {
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
    boolean isError(@NotNull GitHubConnectorResponse connectorResponse) throws IOException {
        return connectorResponse.statusCode() == HTTP_FORBIDDEN
                && "0".equals(connectorResponse.header("X-RateLimit-Remaining"));
    }

    /**
     * Called when the library encounters HTTP error indicating that the API rate limit has been exceeded.
     *
     * <p>
     * Any exception thrown from this method will cause the request to fail, and the caller of github-api will receive
     * an exception. If this method returns normally, another request will be attempted. For that to make sense, the
     * implementation needs to wait for some time.
     *
     * @param connectorResponse
     *            Response information for this request.
     *
     * @throws IOException
     *             the io exception
     * @see <a href="https://developer.github.com/v3/#rate-limiting">API documentation from GitHub</a>
     */
    public abstract void onError(@Nonnull GitHubConnectorResponse connectorResponse) throws IOException;

    /**
     * Wait until the API abuse "wait time" is passed.
     */
    public static final GitHubRateLimitHandler WAIT = new GitHubRateLimitHandler() {
        @Override
        public void onError(GitHubConnectorResponse connectorResponse) throws IOException {
            try {
                Thread.sleep(parseWaitTime(connectorResponse));
            } catch (InterruptedException ex) {
                throw (InterruptedIOException) new InterruptedIOException().initCause(ex);
            }
        }
    };

    /*
     * Exposed for testability. Given an http response, find the rate limit reset header field and parse it. If no
     * header is found, wait for a reasonably amount of time.
     */
    long parseWaitTime(GitHubConnectorResponse connectorResponse) {
        String v = connectorResponse.header("X-RateLimit-Reset");
        if (v == null)
            return Duration.ofMinutes(1).toMillis(); // can't tell, return 1 min

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
        return Math.max(MINIMUM_RATE_LIMIT_RETRY_MILLIS, (Long.parseLong(v) - now.toInstant().getEpochSecond()) * 1000);
    }

    /**
     * Fail immediately.
     */
    public static final GitHubRateLimitHandler FAIL = new GitHubRateLimitHandler() {
        @Override
        public void onError(GitHubConnectorResponse connectorResponse) throws IOException {
            throw new HttpException("API rate limit reached",
                    connectorResponse.statusCode(),
                    connectorResponse.header("Status"),
                    connectorResponse.request().url().toString())
                    .withResponseHeaderFields(connectorResponse.allHeaders());

        }
    };

}
