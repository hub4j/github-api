package org.kohsuke.github.extras;

import org.kohsuke.github.connector.GitHubConnector;
import org.kohsuke.github.connector.GitHubConnectorRequest;
import org.kohsuke.github.connector.GitHubConnectorResponse;

import java.io.IOException;

/**
 * {@link GitHubConnector} wrapper that sets timeout
 *
 * @author Liam Newman
 */
public class HttpClientGitHubConnector implements GitHubConnector {

    /**
     * Instantiates a new Impatient http connector.
     */
    public HttpClientGitHubConnector() {
        throw new UnsupportedOperationException("java.net.http.HttpClient is only supported in Java 11+.");
    }

    @Override
    public GitHubConnectorResponse send(GitHubConnectorRequest connectorRequest) throws IOException {
        throw new UnsupportedOperationException("java.net.http.HttpClient is only supported in Java 11+.");
    }
}
