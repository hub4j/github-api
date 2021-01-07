package org.kohsuke.github;

import java.io.IOException;

/**
 * Provides a functional interface that returns a valid encodedAuthorization. This strategy allows for a provider that
 * dynamically changes the credentials. Each request will request the credentials from the provider.
 */
public interface AuthorizationProvider {
    /**
     * An static instance for an ANONYMOUS authorization provider
     */
    AuthorizationProvider ANONYMOUS = new AnonymousAuthorizationProvider();

    /**
     * Returns the credentials to be used with a given request. As an example, a authorization provider for a bearer
     * token will return something like:
     *
     * <pre>
     * {@code
     *  &#64;Override
     *  public String getEncodedAuthorization() {
     *  return "Bearer myBearerToken";
     *  }
     * }
     * </pre>
     *
     * @return encoded authorization string, can be null
     * @throws IOException
     *             on any error that prevents the provider from getting a valid authorization
     */
    String getEncodedAuthorization() throws IOException;

    /**
     * Binds this authorization provider to a github instance.
     *
     * Only needs to be implemented by dynamic credentials providers that use a github instance in order to refresh.
     *
     * @param github
     *            The github instance to be used for refreshing dynamic credentials
     */
    default void bind(GitHub github) {
    }

    /**
     * A {@link AuthorizationProvider} that ensures that no credentials are returned
     */
    class AnonymousAuthorizationProvider implements AuthorizationProvider {
        @Override
        public String getEncodedAuthorization() throws IOException {
            return null;
        }
    }
}
