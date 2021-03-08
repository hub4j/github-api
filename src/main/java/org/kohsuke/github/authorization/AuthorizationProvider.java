package org.kohsuke.github.authorization;

import java.io.IOException;
import java.net.URL;

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
    String getEncodedAuthorization(URL url) throws IOException;

    /**
     * A {@link AuthorizationProvider} that ensures that no credentials are returned
     */
    class AnonymousAuthorizationProvider implements AuthorizationProvider {
        @Override
        public String getEncodedAuthorization() throws IOException {
            return getEncodedAuthorization(null);
        }
        @Override
        public String getEncodedAuthorization(URL url) throws IOException {
            return null;
        }
    }
}
