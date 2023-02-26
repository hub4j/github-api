package org.kohsuke.github;

import org.junit.Test;
import org.kohsuke.github.authorization.ImmutableAuthorizationProvider;
import org.kohsuke.github.authorization.OrgAppInstallationAuthorizationProvider;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

// TODO: Auto-generated Javadoc
/**
 * The Class OrgAppInstallationAuthorizationProviderTest.
 */
public class OrgAppInstallationAuthorizationProviderTest extends AbstractGHAppInstallationTest {

    /**
     * Instantiates a new org app installation authorization provider test.
     */
    public OrgAppInstallationAuthorizationProviderTest() {
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
        OrgAppInstallationAuthorizationProvider provider = new OrgAppInstallationAuthorizationProvider("hub4j-test-org",
                ImmutableAuthorizationProvider.fromJwtToken("bogus-valid-token"));
        gitHub = getGitHubBuilder().withAuthorizationProvider(provider)
                .withEndpoint(mockGitHub.apiServer().baseUrl())
                .build();
        String encodedAuthorization = provider.getEncodedAuthorization();

        assertThat(encodedAuthorization, notNullValue());
        assertThat(encodedAuthorization, equalTo("token v1.9a12d913f980a45a16ac9c3a9d34d9b7sa314cb6"));
    }

}
