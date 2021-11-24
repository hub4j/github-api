package org.kohsuke.github;

import org.kohsuke.github.connector.GitHubConnectorResponse;

import java.io.IOException;
import java.net.HttpURLConnection;

import javax.annotation.Nonnull;

/**
 * Pluggable strategy to determine what to do when the API rate limit is reached.
 *
 * @author Kohsuke Kawaguchi
 * @see GitHubBuilder#withAbuseLimitHandler(AbuseLimitHandler) GitHubBuilder#withRateLimitHandler(AbuseLimitHandler)
 * @see GitHubRateLimitHandler
 *
 * @author Liam Newman
 */
public abstract class GitHubAbuseLimitHandler extends GitHubConnectorResponseErrorHandler {

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
}
