package org.kohsuke.github.connector;

import org.kohsuke.github.GHIOException;
import org.kohsuke.github.internal.DefaultGitHubConnector;

import java.io.IOException;

/**
 * Interface for customizing HTTP request behaviors or using any HTTP client library for interacting with GitHub.
 *
 * @author Liam Newman
 */
@FunctionalInterface
public interface GitHubConnector {

    /**
     * Sends a request and retrieves a raw response for processing.
     *
     * Implementers of {@link GitHubConnector#send(GitHubConnectorRequest)} process the information from a
     * {@link GitHubConnectorRequest} to open an HTTP connection and retrieve a raw response. They then return a class
     * that extends {@link GitHubConnectorResponse} corresponding their response data.
     *
     * Clients should not implement their own {@link GitHubConnectorRequest}. The {@link GitHubConnectorRequest}
     * provided by the caller of {@link GitHubConnector#send(GitHubConnectorRequest)} should be passed to the
     * constructor of {@link GitHubConnectorResponse}.
     *
     * @param connectorRequest
     *            the request data to be sent.
     * @return a GitHubConnectorResponse for the request
     * @throws IOException
     *             if there is an I/O error
     *
     * @author Liam Newman
     */
    GitHubConnectorResponse send(GitHubConnectorRequest connectorRequest) throws IOException;

    /**
     * Default implementation used when connector is not set by user.
     *
     * This calls {@link DefaultGitHubConnector#create()} to get the default connector instance. The output of that
     * method may differ depending on Java version and system properties.
     *
     * @see DefaultGitHubConnector#create() DefaultGitHubConnector#create()
     */
    GitHubConnector DEFAULT = DefaultGitHubConnector.create();

    /**
     * Stub implementation that is always off-line.
     */
    GitHubConnector OFFLINE = new GitHubConnector() {
        @Override
        public GitHubConnectorResponse send(GitHubConnectorRequest connectorRequest) throws IOException {
            throw new GHIOException("Offline");
        }
    };
}
