package org.kohsuke.github_api.v2.internal;

import org.junit.Assert;
import org.junit.Test;
import org.kohsuke.github_api.v2.AbstractGitHubWireMockTest;
import org.kohsuke.github_api.v2.GitHubBuilder;
import org.kohsuke.github_api.v2.connector.GitHubConnector;
import org.kohsuke.github_api.v2.connector.GitHubConnectorRequest;
import org.kohsuke.github_api.v2.connector.GitHubConnectorResponse;
import org.kohsuke.github_api.v2.extras.HttpClientGitHubConnector;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.*;

// TODO: Auto-generated Javadoc
/**
 * The Class DefaultGitHubConnectorTest.
 */
public class DefaultGitHubConnectorTest extends AbstractGitHubWireMockTest {

    /**
     * Instantiates a new default git hub connector test.
     */
    public DefaultGitHubConnectorTest() {
        useDefaultGitHub = false;
    }

    /**
     * Test create.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testCreate() throws Exception {
        GitHubConnector connector;

        connector = DefaultGitHubConnector.create("httpclient");
        assertThat(connector, instanceOf(HttpClientGitHubConnector.class));

        connector = DefaultGitHubConnector.create("default");

        assertThat(connector, instanceOf(HttpClientGitHubConnector.class));

        connector = DefaultGitHubConnector.create("okhttp");

        Assert.assertThrows(IllegalStateException.class, () -> DefaultGitHubConnector.create(""));

        gitHub = new GitHubBuilder().withConnector(new GitHubConnector() {
            @Override
            public GitHubConnectorResponse send(GitHubConnectorRequest connectorRequest) throws IOException {
                throw new IOException();
            }
        }).build();
    }
}
