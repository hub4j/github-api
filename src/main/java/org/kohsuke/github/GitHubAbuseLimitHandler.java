package org.kohsuke.github;

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
    boolean isError(@Nonnull GitHubConnectorResponse connectorResponse) {
        return isForbidden(connectorResponse) && hasRetryOrLimitHeader(connectorResponse);
    }

    /**
     * Checks if the response status code is HTTP_FORBIDDEN (403).
     *
     * @param connectorResponse
     *            the response from the GitHub connector
     * @return true if the status code is HTTP_FORBIDDEN
     */
    private boolean isForbidden(GitHubConnectorResponse connectorResponse) {
        return connectorResponse.statusCode() == HttpURLConnection.HTTP_FORBIDDEN;
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
     *      "https://docs.github.com/en/rest/using-the-rest-api/best-practices-for-using-the-rest-api?apiVersion=2022-11-28#handle-rate-limit-errors-appropriately</a>
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
}
