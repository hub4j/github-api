package org.kohsuke.github;

import org.jetbrains.annotations.NotNull;
import org.kohsuke.github.connector.GitHubConnectorResponse;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.annotation.Nonnull;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;

// TODO: Auto-generated Javadoc
/**
 * Pluggable strategy to detect and choose what to do when errors occur during an http request.
 *
 * @author Liam Newman
 */
abstract class GitHubConnectorResponseErrorHandler {

    /**
     * Called to detect an error handled by this handler.
     *
     * @param connectorResponse
     *            the connector response
     * @return {@code true} if there is an error and {@link #onError(GitHubConnectorResponse)} should be called
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    abstract boolean isError(@Nonnull GitHubConnectorResponse connectorResponse) throws IOException;

    /**
     * Called when the library encounters HTTP error matching {@link #isError(GitHubConnectorResponse)}
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

    /** The status http bad request or greater. */
    static GitHubConnectorResponseErrorHandler STATUS_HTTP_BAD_REQUEST_OR_GREATER = new GitHubConnectorResponseErrorHandler() {
        @Override
        public boolean isError(@NotNull GitHubConnectorResponse connectorResponse) throws IOException {
            return connectorResponse.statusCode() >= HTTP_BAD_REQUEST;
        }

        @Override
        public void onError(@NotNull GitHubConnectorResponse connectorResponse) throws IOException {
            if (connectorResponse.statusCode() == HTTP_NOT_FOUND) {
                throw new FileNotFoundException(connectorResponse.request().url().toString());
            } else {
                throw new HttpException(connectorResponse);
            }
        }
    };
}
