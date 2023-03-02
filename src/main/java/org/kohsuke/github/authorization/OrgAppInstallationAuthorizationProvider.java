package org.kohsuke.github.authorization;

import org.kohsuke.github.BetaApi;

/**
 * An AuthorizationProvider that performs automatic token refresh for an organization's AppInstallation.
 */
@Deprecated
public class OrgAppInstallationAuthorizationProvider extends AppInstallationAuthorizationProvider {

    /**
     * Provides an AuthorizationProvider that performs automatic token refresh, based on an previously authenticated
     * github client.
     *
     * @param organizationName
     *            The name of the organization where the application is installed
     * @param authorizationProvider
     *            A authorization provider that returns a JWT token that can be used to refresh the App Installation
     *            token from GitHub.
     *
     * @deprecated Replaced by {@link #AppInstallationAuthorizationProvider}
     */
    @BetaApi
    @Deprecated
    public OrgAppInstallationAuthorizationProvider(String organizationName,
            AuthorizationProvider authorizationProvider) {
        super(app -> app.getInstallationByOrganization(organizationName), authorizationProvider);
    }
}
