package org.kohsuke.github;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.annotation.CheckForNull;

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
     *            The token
     * @return a correctly configured {@link CredentialProvider} that will always return the same provided
     *         oauthAccessToken
     */
    public static CredentialProvider fromOauthToken(String oauthAccessToken) {
        return new UserCredentialProvider(String.format("token %s", oauthAccessToken));
    }

    /**
     * Builds and returns a {@link CredentialProvider} from a given oauthAccessToken
     *
     * @param oauthAccessToken
     *            The token
     * @return a correctly configured {@link CredentialProvider} that will always return the same provided
     *         oauthAccessToken
     */
    public static CredentialProvider fromOauthToken(String oauthAccessToken, String login) {
        return new UserCredentialProvider(String.format("token %s", oauthAccessToken), login);
    }

    /**
     * Builds and returns a {@link CredentialProvider} from a given App Installation Token
     *
     * @param appInstallationToken
     *            A string containing the GitHub App installation token
     * @return the configured Builder from given GitHub App installation token.
     */
    public static CredentialProvider fromAppInstallationToken(String appInstallationToken) {
        return fromOauthToken(appInstallationToken, "");
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
     * @deprecated Login with password credentials are no longer supported by GitHub
     */
    @Deprecated
    public static CredentialProvider fromLoginAndPassword(String login, String password) {
        try {
            String authorization = (String.format("%s:%s", login, password));
            String charsetName = StandardCharsets.UTF_8.name();
            String b64encoded = Base64.getEncoder().encodeToString(authorization.getBytes(charsetName));
            String encodedAuthorization = String.format("Basic %s", b64encoded);
            return new UserCredentialProvider(encodedAuthorization, login);
        } catch (UnsupportedEncodingException e) {
            // If UTF-8 isn't supported, there are bigger problems
            throw new IllegalStateException("Could not generate encoded authorization", e);
        }
    }

    @Override
    public String getEncodedAuthorization() {
        return this.authorization;
    }

    /**
     * An internal class representing all user-related credentials, which are credentials that have a login or should
     * query the user endpoint for the login matching this credential.
     */
    static class UserCredentialProvider extends ImmutableCredentialProvider {

        private final String login;

        UserCredentialProvider(String authorization) {
            this(authorization, null);
        }

        UserCredentialProvider(String authorization, String login) {
            super(authorization);
            this.login = login;
        }

        @CheckForNull
        String getLogin() {
            return login;
        }

    }
}
