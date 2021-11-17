package org.kohsuke.github.extras;

import org.kohsuke.github.connector.GitHubConnector;
import org.kohsuke.github.connector.GitHubConnectorRequest;
import org.kohsuke.github.connector.GitHubConnectorResponse;

import java.io.IOException;
import java.net.http.HttpClient;

/**
 * {@link GitHubConnector} wrapper that sets timeout
 *
 * @author Liam Newman
 */
public class HttpClientGitHubConnector implements GitHubConnector {

    private final HttpClient client;

    /**
     * Instantiates a new Impatient http connector.
     *
     * @param client
     *            the base
     */
    public HttpClientGitHubConnector(HttpClient client) {
        this.client = client;
    }

    @Override
    public GitHubConnectorResponse send(GitHubConnectorRequest connectorRequest) throws IOException {
        return null;
    }
}
