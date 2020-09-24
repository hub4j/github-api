package org.kohsuke.github;

/**
 * Provides a functional interface that returns a valid encodedAuthorization. This strategy allows
 * for a provider that dynamically changes the credentials. Each request will request the credentials
 * from the provider.
 */
public interface CredentialProvider {
    /**
     * Returns the credentials to be used with a given request. As an example, a credential
     * provider for a bearer token will return something like:
     * <pre>{@code
     *  @Override
     *  public String getEncodedAuthorization() {
     *  return "Bearer myBearerToken";
     *  }
     * }</pre>
     *
     * @return encoded authorization string, can be null
     */
    String getEncodedAuthorization();
}
