package org.kohsuke.github;

import org.jetbrains.annotations.NotNull;
import org.kohsuke.github.connector.GitHubConnectorResponse;

import java.io.IOException;
import java.net.HttpURLConnection;

import javax.annotation.Nonnull;

// TODO: Auto-generated Javadoc
/**
 * Pluggable strategy to determine what to do when the API rate limit is reached.
 *
 * @author Kohsuke Kawaguchi
 * @author Liam Newman
 * @see GitHubBuilder#withRateLimitHandler(RateLimitHandler) GitHubBuilder#withRateLimitHandler(RateLimitHandler)
 * @see GitHubAbuseLimitHandler
 */
public abstract class GitHubRateLimitHandler extends GitHubConnectorResponseErrorHandler {

    /**
     * Checks if is error.
     *
     * @param connectorResponse the connector response
     * @return true, if is error
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    boolean isError(@NotNull GitHubConnectorResponse connectorResponse) throws IOException {
        return connectorResponse.statusCode() == HttpURLConnection.HTTP_FORBIDDEN
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
}
