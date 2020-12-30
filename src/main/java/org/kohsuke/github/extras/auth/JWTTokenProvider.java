package org.kohsuke.github.extras.auth;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.kohsuke.github.CredentialProvider;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

import javax.annotation.Nonnull;

/**
 * A credential provider that gives valid JWT tokens. These tokens are then used to create a time-based token to
 * authenticate as an application. This token provider does not provide any kind of caching, and will always request a
 * new token to the API.
 */
public class JWTTokenProvider implements CredentialProvider {

    private static final long MINUTES_10 = Duration.ofMinutes(10).toMillis();

    private final PrivateKey privateKey;

    @Nonnull
    private Instant validUntil = Instant.MIN;

    private String token;

    /**
     * The identifier for the application
     */
    private final String applicationId;

    public JWTTokenProvider(String applicationId, File keyFile) throws GeneralSecurityException, IOException {
        this(applicationId, loadPrivateKey(keyFile.toPath()));
    }

    public JWTTokenProvider(String applicationId, Path keyPath) throws GeneralSecurityException, IOException {
        this(applicationId, loadPrivateKey(keyPath));
    }

    public JWTTokenProvider(String applicationId, PrivateKey privateKey) {
        this.privateKey = privateKey;
        this.applicationId = applicationId;
    }

    @Override
    public String getEncodedAuthorization() throws IOException {
        synchronized (this) {
            if (Instant.now().isAfter(validUntil)) {
                token = refreshJWT();
            }
            return token;
        }
    }

    /**
     * add dependencies for a jwt suite You can generate a key to load in this method with:
     *
     * <pre>
     * openssl pkcs8 -topk8 -inform PEM -outform DER -in ~/github-api-app.private-key.pem -out ~/github-api-app.private-key.der -nocrypt
     * </pre>
     */
    private static PrivateKey loadPrivateKey(Path keyPath) throws GeneralSecurityException, IOException {
        String keyString = new String(Files.readAllBytes(keyPath), StandardCharsets.UTF_8);
        return getPrivateKeyFromString(keyString);
    }

    /**
     * Convert a PKCS#8 formatted private key in string format into a java PrivateKey
     *
     * @param key
     *            PCKS#8 string
     * @return private key
     * @throws GeneralSecurityException
     *             if we couldn't parse the string
     */
    private static PrivateKey getPrivateKeyFromString(final String key) throws GeneralSecurityException {
        if (key.contains(" RSA ")) {
            throw new InvalidKeySpecException(
                    "Private key must be a PKCS#8 formatted string, to convert it from PKCS#1 use: "
                            + "openssl pkcs8 -topk8 -inform PEM -outform PEM -in current-key.pem -out new-key.pem -nocrypt");
        }

        // Remove all comments and whitespace from PEM
        // such as "-----BEGIN PRIVATE KEY-----" and newlines
        String privateKeyContent = key.replaceAll("(?m)^--.*", "").replaceAll("\\s", "");

        KeyFactory kf = KeyFactory.getInstance("RSA");

        try {
            byte[] decode = Base64.getDecoder().decode(privateKeyContent);
            PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(decode);

            return kf.generatePrivate(keySpecPKCS8);
        } catch (IllegalArgumentException e) {
            throw new InvalidKeySpecException("Failed to decode private key: " + e.getMessage(), e);
        }
    }

    private String refreshJWT() {
        Instant now = Instant.now();

        // Token expires in 10 minutes
        Instant expiration = Instant.now().plus(Duration.ofMinutes(10));

        // Let's set the JWT Claims
        JwtBuilder builder = Jwts.builder()
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiration))
                .setIssuer(this.applicationId)
                .signWith(privateKey, SignatureAlgorithm.RS256);

        // Token will refresh after 8 minutes
        validUntil = expiration.minus(Duration.ofMinutes(2));

        // Builds the JWT and serializes it to a compact, URL-safe string
        return builder.compact();
    }
}
