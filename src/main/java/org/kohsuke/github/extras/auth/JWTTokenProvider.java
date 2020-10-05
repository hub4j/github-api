package org.kohsuke.github.extras.auth;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.kohsuke.github.CredentialProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.util.Date;

/**
 * A credential provider that gives valid JWT tokens. These tokens are then used to create a time-based token to
 * authenticate as an application. This token provider does not provide any kind of caching, and will always request a
 * new token to the API.
 */
public class JWTTokenProvider implements CredentialProvider {

    private static final long MINUTES_10 = Duration.ofMinutes(10).toMillis();

    private final PrivateKey privateKey;

    /**
     * The identifier for the application
     */
    private final String applicationId;

    public JWTTokenProvider(String applicationId, Path keyPath)
            throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {
        this.privateKey = loadPrivateKey(keyPath);
        this.applicationId = applicationId;
    }

    /**add dependencies for a jwt suite
     * You can generate a key to load with this method with:
     *
     * <pre>
     * openssl pkcs8 -topk8 -inform PEM -outform DER -in ~/github-api-app.private-key.pem -out ~/github-api-app.private-key.der -nocrypt
     * </pre>
     */
    private PrivateKey loadPrivateKey(Path keyPath)
            throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {

        byte[] keyBytes = Files.readAllBytes(keyPath);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    public String getJWT() {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        // Let's set the JWT Claims
        JwtBuilder builder = Jwts.builder()
                .setIssuedAt(now)
                .setIssuer(this.applicationId)
                .signWith(privateKey, SignatureAlgorithm.RS256);

        // if it has been specified, let's add the expiration
        if (MINUTES_10 > 0) {
            long expMillis = nowMillis + MINUTES_10;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp);
        }

        // Builds the JWT and serializes it to a compact, URL-safe string
        return builder.compact();
    }

    @Override
    public String getEncodedAuthorization() throws IOException {
        return getJWT();
    }

}
