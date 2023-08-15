package org.kohsuke.github.extras.authorization;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.Test;
import org.kohsuke.github.AbstractGitHubWireMockTest;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.RateLimitHandler;
import org.kohsuke.github.authorization.AuthorizationProvider;

import java.io.IOException;

/**
 * Test authorization token refresh.
 */
public class AuthorizationTokenRefreshTest extends AbstractGitHubWireMockTest {

    /**
     * Instantiates a new test.
     */
    public AuthorizationTokenRefreshTest() throws IOException {
        useDefaultGitHub = false;
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

    /**
     * Retried request should get new token when the old one expires.
     */
    @Test
    public void testRetriedRequestGetsNewAuthorizationTokenWhenOldOneExpires() throws IOException {
        snapshotNotAllowed();
        gitHub = getGitHubBuilder().withAuthorizationProvider(new RefreshingAuthorizationProvider())
                .withEndpoint(mockGitHub.apiServer().baseUrl())
                .withRateLimitHandler(RateLimitHandler.WAIT)
                .build();
        final GHUser kohsuke = gitHub.getUser("kohsuke");
        assertThat("Usernames match", "kohsuke".equals(kohsuke.getLogin()));
    }

    /**
     * Retried request should not get new token when the old one is still valid.
     */
    @Test
    public void testRetriedRequestDoesNotGetNewAuthorizationTokenWhenOldOneIsStillValid() throws IOException {
        gitHub = getGitHubBuilder().withAuthorizationProvider(() -> "original token")
                .withEndpoint(mockGitHub.apiServer().baseUrl())
                .withRateLimitHandler(RateLimitHandler.WAIT)
                .build();
        final GHUser kohsuke = gitHub.getUser("kohsuke");
        assertThat("Usernames match", "kohsuke".equals(kohsuke.getLogin()));
    }

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
}
