package org.kohsuke.github;

import io.jsonwebtoken.Jwts;
import org.apache.commons.io.IOUtils;
import org.kohsuke.github.authorization.AuthorizationProvider;
import org.kohsuke.github.extras.authorization.JWTTokenProvider;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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

    protected final AuthorizationProvider jwtProvider1;
    protected final AuthorizationProvider jwtProvider2;
    protected final AuthorizationProvider jwtProvider3;

    protected AbstractGHAppInstallationTest() {
        try {
            jwtProvider1 = new JWTTokenProvider(TEST_APP_ID_1,
                    new File(this.getClass().getResource(PRIVATE_KEY_FILE_APP_1).getFile()));
            jwtProvider2 = new JWTTokenProvider(TEST_APP_ID_2,
                    new File(this.getClass().getResource(PRIVATE_KEY_FILE_APP_2).getFile()).toPath());
            jwtProvider3 = new JWTTokenProvider(TEST_APP_ID_3,
                    new String(
                            Files.readAllBytes(
                                    new File(this.getClass().getResource(PRIVATE_KEY_FILE_APP_3).getFile()).toPath()),
                            StandardCharsets.UTF_8));
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

    protected GHAppInstallation getAppInstallationWithToken(String jwtToken) throws IOException {
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

        // TODO: this is odd
        // appInstallation
        // .setRoot(getGitHubBuilder().withAppInstallationToken(appInstallation.createToken().create().getToken())
        // .withEndpoint(mockGitHub.apiServer().baseUrl())
        // .build());

        return appInstallation;
    }

}
