package org.kohsuke.github.authorization;

import java.io.IOException;

/**
 * A {@link AuthorizationProvider} that returns an empty authorization.
 * <p>
 * This will result in the "Authorization" header not being added to a request.
 */
public class AnonymousAuthorizationProvider implements AuthorizationProvider {

    /**
     * Create default AnonymousAuthorizationProvider instance
     */
    public AnonymousAuthorizationProvider() {
    }

    @Override
    public String getEncodedAuthorization() throws IOException {
        return null;
    }
}
