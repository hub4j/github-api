package org.kohsuke.github.extras.authorization;

import org.junit.Test;
import org.kohsuke.github.*;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.time.Instant;

import static org.hamcrest.Matchers.*;

/*
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
public class JWTTokenProviderTest extends AbstractGHAppInstallationTest {

    private static String TEST_APP_ID_2 = "83009";
    private static String PRIVATE_KEY_FILE_APP_2 = "/ghapi-test-app-2.private-key.pem";

    @Test
    public void testCachingValidAuthorization() throws IOException {
        assertThat(jwtProvider1, instanceOf(JWTTokenProvider.class));
        JWTTokenProvider provider = (JWTTokenProvider) jwtProvider1;

        assertThat(provider.isNotValid(), is(true));
        String authorization = provider.getEncodedAuthorization();
        assertThat(provider.isNotValid(), is(false));
        String authorizationRefresh = provider.getEncodedAuthorization();
        assertThat(authorizationRefresh, sameInstance(authorization));
    }

    @Test
    public void testAuthorizationHeaderPattern() throws GeneralSecurityException, IOException {
        // authorization header check is custom
        snapshotNotAllowed();

        JWTTokenProvider jwtTokenProvider = new JWTTokenProvider(TEST_APP_ID_2,
                new File(this.getClass().getResource(PRIVATE_KEY_FILE_APP_2).getFile()));
        GitHub gh = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl())
                .withAuthorizationProvider(jwtTokenProvider)
                .build();

        // Request the application, the wiremock matcher will ensure that the header
        // for the authorization is present and has a the format of a valid JWT token
        gh.getApp();
    }

    @Test
    public void testIssuedAtSkew() throws GeneralSecurityException, IOException {
        // TODO: This isn't a great test as it doesn't really check anything in CI
        // This test was accurate when recorded but it doesn't verify that the jwt token is different
        // or accurate in anyway.

        JWTTokenProvider jwtTokenProvider = new JWTTokenProvider(TEST_APP_ID_2,
                new File(this.getClass().getResource(PRIVATE_KEY_FILE_APP_2).getFile())) {

            @Override
            Instant getIssuedAt(Instant now) {
                return now.plus(Duration.ofMinutes(2));
            }
        };
        GitHub gh = getGitHubBuilder().withEndpoint(mockGitHub.apiServer().baseUrl())
                .withAuthorizationProvider(jwtTokenProvider)
                .build();

        try {
            // Request the application, the wiremock matcher will ensure that the header
            // for the authorization is present and has a the format of a valid JWT token
            gh.getApp();
            fail();
        } catch (HttpException e) {
            assertThat(e.getResponseCode(), equalTo(401));
            assertThat(e.getMessage(),
                    containsString(
                            "'Issued at' claim ('iat') must be an Integer representing the time that the assertion was issued"));
        }
    }

}
