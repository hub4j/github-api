package org.kohsuke.github.authorization;

import java.time.Instant;

/**
 * Represents the credentials obtained during the GitHub device flow authorization process. Contains access token,
 * refresh token, expiration times, scope, and token type. The content of this object should be treated as sensitive
 * information and be properly and securely stored.
 */
public class DeviceFlowGithubAppCredentials {
    /**
     * A constant representing empty credentials. Probably what you want to use on the first call before you have any
     * credentials persisted.
     */
    public static final DeviceFlowGithubAppCredentials EMPTY_CREDENTIALS = new DeviceFlowGithubAppCredentials();

    private String accessToken;
    private Instant expiresIn;
    private String refreshToken;
    private Instant refreshTokenExpiresIn;
    // should be empty
    private String scope;
    // should be Bearer
    private String tokenType;

    /**
     * Default constructor for creating an empty credentials object.
     */
    public DeviceFlowGithubAppCredentials() {
        // empty
    }

    /**
     * Gets the access token.
     *
     * @return The access token.
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Gets the expiration time of the access token.
     *
     * @return The expiration time as an {@link Instant}.
     */
    public Instant getExpiresIn() {
        return expiresIn;
    }

    /**
     * Gets the refresh token.
     *
     * @return The refresh token.
     */
    public String getRefreshToken() {
        return refreshToken;
    }

    /**
     * Gets the expiration time of the refresh token.
     *
     * @return The expiration time as an {@link Instant}.
     */
    public Instant getRefreshTokenExpiresIn() {
        return refreshTokenExpiresIn;
    }

    /**
     * Gets the scope of the credentials.
     *
     * @return The scope, which should be empty.
     */
    public String getScope() {
        return scope;
    }

    /**
     * Gets the token type.
     *
     * @return The token type, which should be "Bearer".
     */
    public String getTokenType() {
        return tokenType;
    }

    /**
     * Sets the access token.
     *
     * @param accessToken
     *            The access token to set.
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * Sets the expiration time of the access token.
     *
     * @param expiresIn
     *            The expiration time as an {@link Instant}.
     */
    public void setExpiresIn(Instant expiresIn) {
        this.expiresIn = expiresIn;
    }

    /**
     * Sets the refresh token.
     *
     * @param refreshToken
     *            The refresh token to set.
     */
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    /**
     * Sets the expiration time of the refresh token.
     *
     * @param refreshTokenExpiresIn
     *            The expiration time as an {@link Instant}.
     */
    public void setRefreshTokenExpiresIn(Instant refreshTokenExpiresIn) {
        this.refreshTokenExpiresIn = refreshTokenExpiresIn;
    }

    /**
     * Sets the scope of the credentials.
     *
     * @param scope
     *            The scope to set, which should be empty.
     */
    public void setScope(String scope) {
        this.scope = scope;
    }

    /**
     * Sets the token type.
     *
     * @param tokenType
     *            The token type to set, which should be "Bearer".
     */
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    /**
     * Converts the credentials to an encoded authorization string.
     *
     * @return The encoded authorization string in the format "Bearer {accessToken}".
     */
    public String toEncodedCredentials() {
        return String.format("Bearer %s", getAccessToken());
    }
}
