package org.kohsuke.github;

import org.junit.Test;
import org.kohsuke.github.internal.DefaultGitHubJackson;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.*;

/**
 * Tests for Jackson implementation selection via {@link GitHubBuilder#useJackson2()}.
 */
public class GitHubJacksonTest extends AbstractGitHubWireMockTest {

    /**
     * Create default GitHubJacksonTest instance.
     */
    public GitHubJacksonTest() {
        useDefaultGitHub = false;
    }

    /**
     * Test that the default Jackson implementation is Jackson 3 when available.
     *
     * @throws IOException
     *             the io exception
     */
    @Test
    public void testDefaultJacksonIsJackson3WhenAvailable() throws IOException {
        gitHub = getGitHubBuilder().build();
        String implementationName = gitHub.getClient().getJacksonImplementationName();
        if (DefaultGitHubJackson.isJackson3Available()) {
            assertThat(implementationName, startsWith("Jackson 3."));
        } else {
            assertThat(implementationName, startsWith("Jackson 2."));
        }
    }

    /**
     * Test that Jackson 2 can be explicitly configured via builder.
     *
     * @throws IOException
     *             the io exception
     */
    @Test
    public void testJackson2ViaBuilder() throws IOException {
        gitHub = getGitHubBuilder().useJackson2().build();
        String implementationName = gitHub.getClient().getJacksonImplementationName();
        assertThat(implementationName, startsWith("Jackson 2."));
    }
}
