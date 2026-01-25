package org.kohsuke.github;

import org.junit.Test;
import org.kohsuke.github.internal.DefaultGitHubJackson;
import org.kohsuke.github.internal.GitHubJackson;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.*;

/**
 * Tests for Jackson implementation selection via {@link GitHubBuilder#withJackson(GitHubJackson)}.
 */
public class GitHubJacksonTest extends AbstractGitHubWireMockTest {

    /**
     * Create default GitHubJacksonTest instance.
     */
    public GitHubJacksonTest() {
        useDefaultGitHub = false;
    }

    /**
     * Test that the default Jackson implementation is Jackson 2.
     *
     * @throws IOException
     *             the io exception
     */
    @Test
    public void testDefaultJacksonIsJackson2() throws IOException {
        gitHub = getGitHubBuilder().build();
        String implementationName = gitHub.getClient().getJacksonImplementationName();
        assertThat(implementationName, startsWith("Jackson 2."));
    }

    /**
     * Test that Jackson 2 can be explicitly configured via builder.
     *
     * @throws IOException
     *             the io exception
     */
    @Test
    public void testJackson2ViaBuilder() throws IOException {
        gitHub = getGitHubBuilder().withJackson(DefaultGitHubJackson.createJackson2()).build();
        String implementationName = gitHub.getClient().getJacksonImplementationName();
        assertThat(implementationName, startsWith("Jackson 2."));
    }

    /**
     * Test that Jackson 3 can be configured via builder when available.
     *
     * @throws IOException
     *             the io exception
     */
    @Test
    public void testJackson3ViaBuilder() throws IOException {
        if (DefaultGitHubJackson.isJackson3Available()) {
            gitHub = getGitHubBuilder().withJackson(DefaultGitHubJackson.createJackson3()).build();
            String implementationName = gitHub.getClient().getJacksonImplementationName();
            assertThat(implementationName, startsWith("Jackson 3."));
        }
    }
}
