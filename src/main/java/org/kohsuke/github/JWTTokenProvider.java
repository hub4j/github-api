package org.kohsuke.github;

/**
 * Functional interface to provide a valid JWT Token. Implementations must ensure that subsequent
 * calls to {@link #token()} <b>always</b> return a valid and up-to-date token
 */
public interface JWTTokenProvider {
    /**
     * Returns a valid JWT token for a given application ID, the JWT token can then be used mostly
     * on a {@link CredentialProvider} to request an API token for a given installation
     *
     * @return a valid JWT token
     */
    String token();
}
