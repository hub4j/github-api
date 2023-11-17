package org.kohsuke.github;

import org.junit.Test;
import org.kohsuke.github.authorization.AppInstallationAuthorizationProvider;
import org.kohsuke.github.authorization.ImmutableAuthorizationProvider;
import org.kohsuke.github.authorization.OrgAppInstallationAuthorizationProvider;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.startsWith;

// TODO: Auto-generated Javadoc

/**
 * The Class AppInstallationAuthorizationProviderTest.
 */
public class AppInstallationAuthorizationProviderTest extends AbstractGHAppInstallationTest {

    /**
     * Instantiates a new org app installation authorization provider test.
     */
    public AppInstallationAuthorizationProviderTest() {
        useDefaultGitHub = false;
    }

    /**
     * Invalid JWT token raises exception.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test(expected = HttpException.class)
    public void invalidJWTTokenRaisesException() throws IOException {
        OrgAppInstallationAuthorizationProvider provider = new OrgAppInstallationAuthorizationProvider(
                "testOrganization",
                ImmutableAuthorizationProvider.fromJwtToken("myToken"));
        gitHub = getGitHubBuilder().withAuthorizationProvider(provider)
                .withEndpoint(mockGitHub.apiServer().baseUrl())
                .build();

        provider.getEncodedAuthorization();
    }

    /**
     * Valid JWT token allows oauth token request.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void validJWTTokenAllowsOauthTokenRequest() throws IOException {
        AppInstallationAuthorizationProvider provider = new AppInstallationAuthorizationProvider(
                app -> app.getInstallationByOrganization("hub4j-test-org"),
                ImmutableAuthorizationProvider.fromJwtToken("bogus-valid-token"));
        gitHub = getGitHubBuilder().withAuthorizationProvider(provider)
                .withEndpoint(mockGitHub.apiServer().baseUrl())
                .build();
        String encodedAuthorization = provider.getEncodedAuthorization();

        assertThat(encodedAuthorization, notNullValue());
        assertThat(encodedAuthorization, equalTo("token v1.9a12d913f980a45a16ac9c3a9d34d9b7sa314cb6"));
    }

    /**
     * Lookup of an app by id works as expected
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void validJWTTokenWhenLookingUpAppById() throws IOException {
        AppInstallationAuthorizationProvider provider = new AppInstallationAuthorizationProvider(
                // https://github.com/organizations/hub4j-test-org/settings/installations/12129901
                app -> app.getInstallationById(12129901L),
                jwtProvider1);
        gitHub = getGitHubBuilder().withAuthorizationProvider(provider)
                .withEndpoint(mockGitHub.apiServer().baseUrl())
                .build();
        String encodedAuthorization = provider.getEncodedAuthorization();

        assertThat(encodedAuthorization, notNullValue());
        // we could assert on the exact token with wiremock, but it would make the update of the test more complex
        // do we really care, getting a token should be enough.
        assertThat(encodedAuthorization, startsWith("token ghs_"));
    }

}
