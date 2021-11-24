package org.kohsuke.github.authorization;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.annotation.CheckForNull;

/**
 * An {@link AuthorizationProvider} that always returns the same credentials.
 */
public class ImmutableAuthorizationProvider implements AuthorizationProvider {

    private final String authorization;

    public ImmutableAuthorizationProvider(String authorization) {
        this.authorization = authorization;
    }

    /**
     * Builds and returns a {@link AuthorizationProvider} from a given oauthAccessToken
     *
     * @param oauthAccessToken
     *            The token
     * @return a correctly configured {@link AuthorizationProvider} that will always return the same provided
     *         oauthAccessToken
     */
    public static AuthorizationProvider fromOauthToken(String oauthAccessToken) {
        return new UserProvider(String.format("token %s", oauthAccessToken));
    }

    /**
     * Builds and returns a {@link AuthorizationProvider} from a given oauthAccessToken
     *
     * @param oauthAccessToken
     *            The token
     * @param login
     *            The login for this token
     *
     * @return a correctly configured {@link AuthorizationProvider} that will always return the same provided
     *         oauthAccessToken
     */
    public static AuthorizationProvider fromOauthToken(String oauthAccessToken, String login) {
        return new UserProvider(String.format("token %s", oauthAccessToken), login);
    }

    /**
     * Builds and returns a {@link AuthorizationProvider} from a given App Installation Token
     *
     * @param appInstallationToken
     *            A string containing the GitHub App installation token
     * @return the configured Builder from given GitHub App installation token.
     */
    public static AuthorizationProvider fromAppInstallationToken(String appInstallationToken) {
        return fromOauthToken(appInstallationToken, "");
    }

    /**
     * Builds and returns a {@link AuthorizationProvider} from a given jwtToken
     *
     * @param jwtToken
     *            The JWT token
     * @return a correctly configured {@link AuthorizationProvider} that will always return the same provided jwtToken
     */
    public static AuthorizationProvider fromJwtToken(String jwtToken) {
        return new ImmutableAuthorizationProvider(String.format("Bearer %s", jwtToken));
    }

    /**
     * Builds and returns a {@link AuthorizationProvider} from the given user/password pair
     *
     * @param login
     *            The login for the user, usually the same as the username
     * @param password
     *            The password for the associated user
     * @return a correctly configured {@link AuthorizationProvider} that will always return the credentials for the same
     *         user and password combo
     * @deprecated Login with password credentials are no longer supported by GitHub
     */
    @Deprecated
    public static AuthorizationProvider fromLoginAndPassword(String login, String password) {
        try {
            String authorization = (String.format("%s:%s", login, password));
            String charsetName = StandardCharsets.UTF_8.name();
            String b64encoded = Base64.getEncoder().encodeToString(authorization.getBytes(charsetName));
            String encodedAuthorization = String.format("Basic %s", b64encoded);
            return new UserProvider(encodedAuthorization, login);
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
     *
     * @see org.kohsuke.github.authorization.UserAuthorizationProvider UserAuthorizationProvider
     */
    private static class UserProvider extends ImmutableAuthorizationProvider implements UserAuthorizationProvider {

        private final String login;

        UserProvider(String authorization) {
            this(authorization, null);
        }

        UserProvider(String authorization, String login) {
            super(authorization);
            this.login = login;
        }

        @CheckForNull
        @Override
        public String getLogin() {
            return login;
        }

    }
}
