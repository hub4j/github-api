package org.kohsuke.github.extras;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.kohsuke.github.connector.GitHubConnector;
import org.kohsuke.github.connector.GitHubConnectorRequest;
import org.kohsuke.github.connector.GitHubConnectorResponse;

import java.io.IOException;

/**
 * {@link GitHubConnector} for platforms that do not support Java 11 HttpClient.
 *
 * @author Liam Newman
 */
@SuppressFBWarnings(value = { "CT_CONSTRUCTOR_THROW" }, justification = "Basic validation")
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
