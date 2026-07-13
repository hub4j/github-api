package org.kohsuke.github.internal;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Tests for {@link DefaultGitHubJackson} factory.
 */
public class DefaultGitHubJacksonTest {

    /**
     * Create default DefaultGitHubJacksonTest instance.
     */
    public DefaultGitHubJacksonTest() {
    }

    /**
     * Test that createDefault returns Jackson 3 when available, otherwise Jackson 2.
     */
    @Test
    public void testCreateDefault() {
        GitHubJackson jackson = DefaultGitHubJackson.createDefault();
        assertThat(jackson, notNullValue());
        if (DefaultGitHubJackson.isJackson3Available()) {
            assertThat(jackson, instanceOf(GitHubJackson3.class));
            assertThat(jackson.getImplementationName(), startsWith("Jackson 3."));
        } else {
            assertThat(jackson, instanceOf(GitHubJackson2.class));
            assertThat(jackson.getImplementationName(), startsWith("Jackson 2."));
        }
    }

    /**
     * Test that createJackson2 returns Jackson 2 implementation.
     */
    @Test
    public void testCreateJackson2() {
        GitHubJackson2 jackson = DefaultGitHubJackson.createJackson2();
        assertThat(jackson, notNullValue());
        assertThat(jackson.getImplementationName(), startsWith("Jackson 2."));
    }

    /**
     * Test that createJackson3 returns Jackson 3 implementation when available.
     */
    @Test
    public void testCreateJackson3WhenAvailable() {
        if (DefaultGitHubJackson.isJackson3Available()) {
            GitHubJackson3 jackson = DefaultGitHubJackson.createJackson3();
            assertThat(jackson, notNullValue());
            assertThat(jackson.getImplementationName(), startsWith("Jackson 3."));
        }
    }

    /**
     * Test Jackson 3 availability check.
     */
    @Test
    public void testJackson3Availability() {
        // Since Jackson 3 is now on the classpath (as optional dependency),
        // it should be available
        boolean available = DefaultGitHubJackson.isJackson3Available();
        assertThat(available, is(true));
    }
}
