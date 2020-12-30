package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

public class OrgInstallationCredentialProviderTest extends AbstractGHAppInstallationTest {

    public OrgInstallationCredentialProviderTest() {
        useDefaultGitHub = false;
    }

    @Test(expected = HttpException.class)
    public void invalidJWTTokenRaisesException() throws IOException {
        OrgInstallationCredentialProvider provider = new OrgInstallationCredentialProvider("testOrganization",
                ImmutableCredentialProvider.fromJwtToken("myToken"));
        gitHub = getGitHubBuilder().withCredentialProvider(provider)
                .withEndpoint(mockGitHub.apiServer().baseUrl())
                .build();

        provider.getEncodedAuthorization();
    }

    @Test
    public void validJWTTokenAllowsOauthTokenRequest() throws IOException {
        OrgInstallationCredentialProvider provider = new OrgInstallationCredentialProvider("hub4j-test-org",
                ImmutableCredentialProvider.fromJwtToken("bogus-valid-token"));
        gitHub = getGitHubBuilder().withCredentialProvider(provider)
                .withEndpoint(mockGitHub.apiServer().baseUrl())
                .build();
        String encodedAuthorization = provider.getEncodedAuthorization();

        assertThat(encodedAuthorization, notNullValue());
        assertThat(encodedAuthorization, equalTo("token v1.9a12d913f980a45a16ac9c3a9d34d9b7sa314cb6"));
    }

}
