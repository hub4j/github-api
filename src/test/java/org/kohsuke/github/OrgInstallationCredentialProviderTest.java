package org.kohsuke.github;

import net.sf.ezmorph.test.ArrayAssertions;
import org.junit.Test;

import java.io.IOException;

public class OrgInstallationCredentialProviderTest extends AbstractGitHubWireMockTest {

    @Test(expected = HttpException.class)
    public void invalidJWTTokenRaisesException() throws IOException {

        gitHub.getClient().credentialProvider = ImmutableCredentialProvider.fromJwtToken("myToken");

        OrgInstallationCredentialProvider provider = new OrgInstallationCredentialProvider("testOrganization", gitHub);

        provider.getEncodedAuthorization();
    }

    @Test
    public void validJWTTokenAllowsOauthTokenRequest() throws IOException {
        gitHub.getClient().credentialProvider = ImmutableCredentialProvider.fromJwtToken("valid-token");

        OrgInstallationCredentialProvider provider = new OrgInstallationCredentialProvider("hub4j-test-org", gitHub);

        String encodedAuthorization = provider.getEncodedAuthorization();

        ArrayAssertions.assertNotNull(encodedAuthorization);
        ArrayAssertions.assertEquals("token v1.9a12d913f980a45a16ac9c3a9d34d9b7sa314cb6", encodedAuthorization);
    }

}
