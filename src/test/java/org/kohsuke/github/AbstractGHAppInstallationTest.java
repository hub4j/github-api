package org.kohsuke.github;

import io.jsonwebtoken.Jwts;
import org.apache.commons.io.IOUtils;
import org.kohsuke.github.authorization.AuthorizationProvider;
import org.kohsuke.github.extras.authorization.JWTTokenProvider;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;

public class AbstractGHAppInstallationTest extends AbstractGitHubWireMockTest {

    private static String TEST_APP_ID_1 = "82994";
    private static String TEST_APP_ID_2 = "83009";
    private static String TEST_APP_ID_3 = "89368";
    private static String PRIVATE_KEY_FILE_APP_1 = "/ghapi-test-app-1.private-key.pem";
    private static String PRIVATE_KEY_FILE_APP_2 = "/ghapi-test-app-2.private-key.pem";
    private static String PRIVATE_KEY_FILE_APP_3 = "/ghapi-test-app-3.private-key.pem";

    private static AuthorizationProvider JWT_PROVIDER_1;
    private static AuthorizationProvider JWT_PROVIDER_2;
    private static AuthorizationProvider JWT_PROVIDER_3;

    AbstractGHAppInstallationTest() {
        try {
            JWT_PROVIDER_1 = new JWTTokenProvider(TEST_APP_ID_1,
                    new File(this.getClass().getResource(PRIVATE_KEY_FILE_APP_1).getFile()));
            JWT_PROVIDER_2 = new JWTTokenProvider(TEST_APP_ID_2,
                    new File(this.getClass().getResource(PRIVATE_KEY_FILE_APP_2).getFile()));
            JWT_PROVIDER_3 = new JWTTokenProvider(TEST_APP_ID_3,
                    new File(this.getClass().getResource(PRIVATE_KEY_FILE_APP_3).getFile()));
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("These should never fail", e);
        }
    }

    private String createJwtToken(String keyFileResouceName, String appId) {
        try {
            String keyPEM = IOUtils.toString(this.getClass().getResource(keyFileResouceName), "US-ASCII")
                    .replaceAll("(?m)^--.*", "") // remove comments from PEM to allow decoding
                    .replaceAll("\\s", "");

            PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(keyPEM));
            PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(keySpecPKCS8);

            return Jwts.builder()
                    .setIssuedAt(Date.from(Instant.now()))
                    .setExpiration(Date.from(Instant.now().plus(5, ChronoUnit.MINUTES)))
                    .setIssuer(appId)
                    .signWith(privateKey)
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException("Error creating JWT token.", e);
        }
    }

    private GHAppInstallation getAppInstallationWithToken(String jwtToken) throws IOException {
        GitHub gitHub = getGitHubBuilder().withJwtToken(jwtToken)
                .withEndpoint(mockGitHub.apiServer().baseUrl())
                .build();

        GHAppInstallation appInstallation = gitHub.getApp()
                .listInstallations()
                .toList()
                .stream()
                .filter(it -> it.getAccount().login.equals("hub4j-test-org"))
                .findFirst()
                .get();

        appInstallation
                .setRoot(getGitHubBuilder().withAppInstallationToken(appInstallation.createToken().create().getToken())
                        .withEndpoint(mockGitHub.apiServer().baseUrl())
                        .build());

        return appInstallation;
    }

    protected GHAppInstallation getAppInstallationWithTokenApp1() throws IOException {
        return getAppInstallationWithToken(JWT_PROVIDER_1.getEncodedAuthorization());
    }

    protected GHAppInstallation getAppInstallationWithTokenApp2() throws IOException {
        return getAppInstallationWithToken(JWT_PROVIDER_2.getEncodedAuthorization());
    }

    protected GHAppInstallation getAppInstallationWithTokenApp3() throws IOException {
        return getAppInstallationWithToken(JWT_PROVIDER_3.getEncodedAuthorization());
    }

}
