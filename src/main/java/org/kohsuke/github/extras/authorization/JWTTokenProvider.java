package org.kohsuke.github.extras.authorization;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.kohsuke.github.authorization.AuthorizationProvider;

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
 * A authorization provider that gives valid JWT tokens. These tokens are then used to create a time-based token to
 * authenticate as an application. This token provider does not provide any kind of caching, and will always request a
 * new token to the API.
 */
public class JWTTokenProvider implements AuthorizationProvider {

    private final PrivateKey privateKey;

    @Nonnull
    private Instant validUntil = Instant.MIN;

    private String token;

    /**
     * The identifier for the application
     */
    private final String applicationId;

    public JWTTokenProvider(String applicationId, File keyFile) throws GeneralSecurityException, IOException {
        this(applicationId, keyFile.toPath());
    }

    public JWTTokenProvider(String applicationId, Path keyPath) throws GeneralSecurityException, IOException {
        this(applicationId, new String(Files.readAllBytes(keyPath), StandardCharsets.UTF_8));
    }

    public JWTTokenProvider(String applicationId, String keyString) throws GeneralSecurityException {
        this(applicationId, getPrivateKeyFromString(keyString));
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
            return String.format("Bearer %s", token);
        }
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

        // Max token expiration is 10 minutes for GitHub
        // We use a smaller window since we likely will not need more than a few seconds
        Instant expiration = now.plus(Duration.ofMinutes(8));

        // Setting the issued at to a time in the past to allow for clock skew
        Instant issuedAt = getIssuedAt(now);

        // Let's set the JWT Claims
        JwtBuilder builder = Jwts.builder()
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(expiration))
                .setIssuer(this.applicationId)
                .signWith(privateKey, SignatureAlgorithm.RS256);

        // Token will refresh 2 minutes before it expires
        validUntil = expiration.minus(Duration.ofMinutes(2));

        // Builds the JWT and serializes it to a compact, URL-safe string
        return builder.compact();
    }

    Instant getIssuedAt(Instant now) {
        return now.minus(Duration.ofMinutes(2));
    }
}
