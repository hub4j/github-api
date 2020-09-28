package org.kohsuke.github;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * A {@link CredentialProvider} that always returns the same credentials
 */
public class ImmutableCredentialProvider implements CredentialProvider {

    private final String authorization;

    public ImmutableCredentialProvider(String authorization) {
        this.authorization = authorization;
    }

    /**
     * Builds and returns a {@link CredentialProvider} from a given oauthAccessToken
     *
     * @param oauthAccessToken
     * @return a correctly configured {@link CredentialProvider} that will always return the same provided
     *         oauthAccessToken
     */
    public static CredentialProvider fromOauthToken(String oauthAccessToken) {
        return new ImmutableCredentialProvider(String.format("token %s", oauthAccessToken));
    }

    /**
     * Builds and returns a {@link CredentialProvider} from a given jwtToken
     *
     * @param jwtToken
     *            The JWT token
     * @return a correctly configured {@link CredentialProvider} that will always return the same provided jwtToken
     */
    public static CredentialProvider fromJwtToken(String jwtToken) {
        return new ImmutableCredentialProvider(String.format("Bearer %s", jwtToken));
    }

    /**
     * Builds and returns a {@link CredentialProvider} from the given user/password pair
     *
     * @param login
     *            The login for the user, usually the same as the username
     * @param password
     *            The password for the associated user
     * @return a correctly configured {@link CredentialProvider} that will always return the credentials for the same
     *         user and password combo
     * @throws UnsupportedEncodingException
     *             the character encoding is not supported
     */
    public static CredentialProvider fromLoginAndPassword(String login, String password)
            throws UnsupportedEncodingException {
        String authorization = (String.format("%s:%s", login, password));
        String charsetName = StandardCharsets.UTF_8.name();
        String b64encoded = Base64.getEncoder().encodeToString(authorization.getBytes(charsetName));
        String encodedAuthorization = String.format("Basic %s", b64encoded);
        return new ImmutableCredentialProvider(encodedAuthorization);
    }

    @Override
    public String getEncodedAuthorization() {
        return this.authorization;
    }
}
