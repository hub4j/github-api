package org.kohsuke.github.extras.auth;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

import org.junit.Test;
import org.kohsuke.github.AbstractGitHubWireMockTest;
import org.kohsuke.github.GitHub;

public class JWTTokenProviderTest extends AbstractGitHubWireMockTest {

    private static String TEST_APP_ID_2 = "83009";
    private static String PRIVATE_KEY_FILE_APP_2 = "/ghapi-test-app-2.private-key.pem";

    /**
     * This test will request an application ensuring that the header for the "Authorization" matches a valid JWT token.
     * A JWT token in the Authorization header will always start with "ey" which is always the start of the base64
     * encoding of the JWT Header , so a valid header will look like this:
     *
     * <pre>
     * Authorization: Bearer ey{rest of the header}.{payload}.{signature}
     * </pre>
     *
     * Matched by the regular expression:
     *
     * <pre>
     * ^Bearer (?<JWTHeader>ey\S*)\.(?<JWTPayload>\S*)\.(?<JWTSignature>\S*)$
     * </pre>
     *
     * Which is present in the wiremock matcher. Note that we need to use a matcher because the JWT token is encoded
     * with a private key and a random nonce, so it will never be the same (under normal conditions). For more
     * information on the format of a JWT token, see: https://jwt.io/introduction/
     */
    @Test
    public void testAuthorizationHeaderPattern() throws GeneralSecurityException, IOException {
        JWTTokenProvider jwtTokenProvider = new JWTTokenProvider(TEST_APP_ID_2,
                new File(this.getClass().getResource(PRIVATE_KEY_FILE_APP_2).getFile()));
        GitHub gh = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl())
                .withAuthorizationProvider(jwtTokenProvider)
                .build();

        // Request the application, the wiremock matcher will ensure that the header
        // for the authorization is present and has a the format of a valid JWT token
        gh.getApp();
    }

}
