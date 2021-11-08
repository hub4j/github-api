package org.kohsuke.github.connector;

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

    GitHubConnectorResponse send(GitHubConnectorRequest request) throws IOException;

    /**
     * Default implementation used when connector is not set by user.
     */
    GitHubConnector DEFAULT = DefaultGitHubConnector.create();

    /**
     * Stub implementation that is always off-line.
     */
    GitHubConnector OFFLINE = new GitHubConnectorHttpConnectorAdapter(url -> {
        throw new IOException("Offline");
    });
}
