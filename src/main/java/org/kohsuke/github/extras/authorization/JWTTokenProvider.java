package org.kohsuke.github.extras.authorization;

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

    private String authorization;

    /**
     * The identifier for the application
     */
    private final String applicationId;

    /**
     * Create a JWTTokenProvider
     *
     * @param applicationId
     *            the application id
     * @param keyFile
     *            the key file
     * @throws GeneralSecurityException
     *             when an error occurs
     * @throws IOException
     *             when an error occurs
     */
    public JWTTokenProvider(String applicationId, File keyFile) throws GeneralSecurityException, IOException {
        this(applicationId, keyFile.toPath());
    }

    /**
     * Create a JWTTokenProvider
     *
     * @param applicationId
     *            the application id
     * @param keyPath
     *            the key path
     * @throws GeneralSecurityException
     *             when an error occurs
     * @throws IOException
     *             when an error occurs
     */
    public JWTTokenProvider(String applicationId, Path keyPath) throws GeneralSecurityException, IOException {
        this(applicationId, new String(Files.readAllBytes(keyPath), StandardCharsets.UTF_8));
    }

    /**
     * Create a JWTTokenProvider
     *
     * @param applicationId
     *            the application id
     * @param keyString
     *            the key string
     * @throws GeneralSecurityException
     *             when an error occurs
     */
    public JWTTokenProvider(String applicationId, String keyString) throws GeneralSecurityException {
        this(applicationId, getPrivateKeyFromString(keyString));
    }

    /**
     * Create a JWTTokenProvider
     *
     * @param applicationId
     *            the application id
     * @param privateKey
     *            the private key
     */
    public JWTTokenProvider(String applicationId, PrivateKey privateKey) {
        this.privateKey = privateKey;
        this.applicationId = applicationId;
    }

    /** {@inheritDoc} */
    @Override
    public String getEncodedAuthorization() throws IOException {
        synchronized (this) {
            if (isNotValid()) {
                String token = refreshJWT();
                authorization = String.format("Bearer %s", token);;
            }
            return authorization;
        }
    }

    /**
     * Indicates whether the token considered valid.
     *
     * <p>
     * This is not the same as whether the token is expired. The token is considered not valid before it actually
     * expires to prevent access denied errors.
     *
     * <p>
     * Made internal for testing
     *
     * @return false if the token has been refreshed within the required window, otherwise true
     */
    boolean isNotValid() {
        return Instant.now().isAfter(validUntil);
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

        // Token will refresh 2 minutes before it expires
        validUntil = expiration.minus(Duration.ofMinutes(2));

        return JwtBuilderUtil.buildJwt(issuedAt, expiration, applicationId, privateKey);
    }

    Instant getIssuedAt(Instant now) {
        return now.minus(Duration.ofMinutes(2));
    }
}
