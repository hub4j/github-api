package org.kohsuke.github;

import org.junit.Test;
import org.kohsuke.github.internal.DefaultGitHubJackson;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.*;

/**
 * Tests for Jackson implementation selection via {@link GitHubBuilder#useJackson3()}.
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
     * Test that Jackson 3 can be configured via builder when available.
     *
     * @throws IOException
     *             the io exception
     */
    @Test
    public void testJackson3ViaBuilder() throws IOException {
        if (DefaultGitHubJackson.isJackson3Available()) {
            gitHub = getGitHubBuilder().useJackson3().build();
            String implementationName = gitHub.getClient().getJacksonImplementationName();
            assertThat(implementationName, startsWith("Jackson 3."));
        }
    }
}
