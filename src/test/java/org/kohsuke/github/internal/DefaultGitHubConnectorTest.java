package org.kohsuke.github.internal;

import org.junit.Assert;
import org.junit.Test;
import org.kohsuke.github.AbstractGitHubWireMockTest;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.connector.GitHubConnector;
import org.kohsuke.github.connector.GitHubConnectorRequest;
import org.kohsuke.github.connector.GitHubConnectorResponse;
import org.kohsuke.github.extras.HttpClientGitHubConnector;

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
