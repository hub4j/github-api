package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

public class OrgInstallationAuthorizationProviderTest extends AbstractGHAppInstallationTest {

    public OrgInstallationAuthorizationProviderTest() {
        useDefaultGitHub = false;
    }

    @Test(expected = HttpException.class)
    public void invalidJWTTokenRaisesException() throws IOException {
        OrgInstallationAuthorizationProvider provider = new OrgInstallationAuthorizationProvider("testOrganization",
                ImmutableAuthorizationProvider.fromJwtToken("myToken"));
        gitHub = getGitHubBuilder().withAuthorizationProvider(provider)
                .withEndpoint(mockGitHub.apiServer().baseUrl())
                .build();

        provider.getEncodedAuthorization();
    }

    @Test
    public void validJWTTokenAllowsOauthTokenRequest() throws IOException {
        OrgInstallationAuthorizationProvider provider = new OrgInstallationAuthorizationProvider("hub4j-test-org",
                ImmutableAuthorizationProvider.fromJwtToken("bogus-valid-token"));
        gitHub = getGitHubBuilder().withAuthorizationProvider(provider)
                .withEndpoint(mockGitHub.apiServer().baseUrl())
                .build();
        String encodedAuthorization = provider.getEncodedAuthorization();

        assertThat(encodedAuthorization, notNullValue());
        assertThat(encodedAuthorization, equalTo("token v1.9a12d913f980a45a16ac9c3a9d34d9b7sa314cb6"));
    }

}
