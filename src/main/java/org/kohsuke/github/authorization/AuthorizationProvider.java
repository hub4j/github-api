package org.kohsuke.github.authorization;

import java.io.IOException;

/**
 * Provides a functional interface that returns a valid encodedAuthorization.
 *
 * This interface support the creation of providers based on immutable credentials or dynamic credentials which change
 * of time. Each {@link org.kohsuke.github.connector.GitHubConnectorRequest} will call
 * {@link #getEncodedAuthorization()} on the provider.
 *
 * @author Liam Newman
 */
public interface AuthorizationProvider {
    /**
     * A static instance for an ANONYMOUS authorization provider
     */
    AuthorizationProvider ANONYMOUS = new AnonymousAuthorizationProvider();

    /**
     * Returns the credentials to be used with a given request. As an example, a authorization provider for a bearer
     * token will return something like:
     *
     * <pre>
     * {@code
     * &#64;Override
     * public String getEncodedAuthorization() {
     *     return "Bearer myBearerToken";
     * }
     * }
     * </pre>
     *
     * @return encoded authorization string, can be null
     * @throws IOException
     *             on any error that prevents the provider from returning a valid authorization
     */
    String getEncodedAuthorization() throws IOException;

}
