package org.kohsuke.github.connector;

import org.kohsuke.github.HttpConnector;
import org.kohsuke.github.internal.DefaultGitHubConnector;
import org.kohsuke.github.internal.GitHubConnectorHttpConnectorAdapter;

import java.io.IOException;

/**
 * Pluggability for customizing HTTP request behaviors or using altogether different library.
 *
 * @author Liam Newman
 */
@FunctionalInterface
public interface GitHubConnector {

    /**
     *
     * Implementers of {@link GitHubConnector#send(GitHubConnectorRequest)} process the information from a
     * {@link GitHubConnectorRequest} to open an HTTP connection and retrieve a response. They then return a class that
     * extends {@link GitHubConnectorResponse} for their response data.
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
     */
    GitHubConnectorResponse send(GitHubConnectorRequest connectorRequest) throws IOException;

    /**
     * Default implementation used when connector is not set by user.
     *
     * This calls {@link DefaultGitHubConnector#create} to get the default connector instance. The output of that method
     * may differ depending on Java version and system properties.
     *
     * @see DefaultGitHubConnector#create
     */
    GitHubConnector DEFAULT = DefaultGitHubConnector.create();

    /**
     * Stub implementation that is always off-line.
     *
     * This connector currently uses {@link GitHubConnectorHttpConnectorAdapter} to maintain backward compatibility as
     * much as possible.
     */
    GitHubConnector OFFLINE = new GitHubConnectorHttpConnectorAdapter(HttpConnector.OFFLINE);
}
