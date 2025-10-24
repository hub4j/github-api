package org.kohsuke.github_api.v2.extras.authorization;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.Test;
import org.kohsuke.github_api.v2.AbstractGitHubWireMockTest;
import org.kohsuke.github_api.v2.GHUser;
import org.kohsuke.github_api.v2.GitHubRateLimitHandler;
import org.kohsuke.github_api.v2.authorization.AuthorizationProvider;

import java.io.IOException;

/**
 * Test authorization token refresh.
 */
public class AuthorizationTokenRefreshTest extends AbstractGitHubWireMockTest {

    static class RefreshingAuthorizationProvider implements AuthorizationProvider {
        private boolean used = false;

        @Override
        public String getEncodedAuthorization() {
            if (used) {
                return "refreshed token";
            }
            used = true;
            return "original token";
        }
    }

    /**
     * Instantiates a new test.
     */
    public AuthorizationTokenRefreshTest() {
        useDefaultGitHub = false;
    }

    /**
     * Retried request should get new token when the old one expires.
     *
     * @throws IOException
     *             the exception
     */
    @Test
    public void testNewWhenOldOneExpires() throws IOException {
        snapshotNotAllowed();
        gitHub = getGitHubBuilder().withAuthorizationProvider(new RefreshingAuthorizationProvider())
                .withEndpoint(mockGitHub.apiServer().baseUrl())
                .withRateLimitHandler(GitHubRateLimitHandler.WAIT)
                .build();
        final GHUser kohsuke = gitHub.getUser("kohsuke");
        assertThat("Usernames match", "kohsuke".equals(kohsuke.getLogin()));
    }

    /**
     * Retried request should not get new token when the old one is still valid.
     *
     * @throws IOException
     *             the exception
     */
    @Test
    public void testNotNewWhenOldOneIsStillValid() throws IOException {
        gitHub = getGitHubBuilder().withAuthorizationProvider(() -> "original token")
                .withEndpoint(mockGitHub.apiServer().baseUrl())
                .withRateLimitHandler(GitHubRateLimitHandler.WAIT)
                .build();
        final GHUser kohsuke = gitHub.getUser("kohsuke");
        assertThat("Usernames match", "kohsuke".equals(kohsuke.getLogin()));
    }

    /**
     * Gets the wire mock options.
     *
     * @return the wire mock options
     */
    @Override
    protected WireMockConfiguration getWireMockOptions() {
        return super.getWireMockOptions().extensions(templating.newResponseTransformer());
    }
}
