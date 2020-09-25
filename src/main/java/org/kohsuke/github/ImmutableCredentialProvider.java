package org.kohsuke.github;

/**
 * A {@link CredentialProvider} that always returns the same credentials
 */
public class ImmutableCredentialProvider implements CredentialProvider {

    private final String authorization;

    public ImmutableCredentialProvider(String authorization) {
        this.authorization = authorization;
    }

    @Override
    public String getEncodedAuthorization() {
        return this.authorization;
    }
}
