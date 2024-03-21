package org.kohsuke.github;

import org.kohsuke.github.connector.GitHubConnectorResponse;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;

import javax.annotation.Nonnull;

// TODO: Auto-generated Javadoc
/**
 * Pluggable strategy to determine what to do when the API rate limit is reached.
 *
 * @author Kohsuke Kawaguchi
 * @author Liam Newman
 * @see GitHubBuilder#withAbuseLimitHandler(AbuseLimitHandler) GitHubBuilder#withRateLimitHandler(AbuseLimitHandler)
 * @see GitHubRateLimitHandler
 */
public abstract class GitHubAbuseLimitHandler extends GitHubConnectorResponseErrorHandler {

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
    boolean isError(@Nonnull GitHubConnectorResponse connectorResponse) throws IOException {
        return connectorResponse.statusCode() == HttpURLConnection.HTTP_FORBIDDEN
                && connectorResponse.header("Retry-After") != null;
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

        private long parseWaitTime(GitHubConnectorResponse connectorResponse) {
            String v = connectorResponse.header("Retry-After");
            if (v == null)
                return 60 * 1000; // can't tell, return 1 min

            return Math.max(1000, Long.parseLong(v) * 1000);
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
}
