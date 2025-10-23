package org.kohsuke.github.authorization;

import javax.annotation.CheckForNull;

/**
 * Interface for all user-related authorization providers.
 *
 * {@link AuthorizationProvider}s can apply to a number of different account types. This interface applies to providers
 * for user accounts, ones that have a login or should query the "/user" endpoint for the login matching this
 * credential.
 */
public interface UserAuthorizationProvider extends AuthorizationProvider {

    /**
     * Gets the user login name.
     *
     * @return the user login for this provider, or {@code null} if the login value should be queried from the "/user"
     *         endpoint.
     */
    @CheckForNull
    String getLogin();
}
