package org.kohsuke.github.authorization;

import org.kohsuke.github.BetaApi;
import org.kohsuke.github.GHAppInstallation;
import org.kohsuke.github.GHAppInstallationToken;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * Provides an AuthorizationProvider that performs automatic token refresh.
 */
public class OrgAppInstallationAuthorizationProvider extends GitHub.DependentAuthorizationProvider {

    private final String organizationName;

    private String latestToken;

    @Nonnull
    private Instant validUntil = Instant.MIN;

    /**
     * Provides an AuthorizationProvider that performs automatic token refresh, based on an previously authenticated
     * github client.
     *
     * @param organizationName
     *            The name of the organization where the application is installed
     * @param authorizationProvider
     *            A authorization provider that returns a JWT token that can be used to refresh the App Installation
     *            token from GitHub.
     */
    @BetaApi
    @Deprecated
    public OrgAppInstallationAuthorizationProvider(String organizationName,
            AuthorizationProvider authorizationProvider) {
        super(authorizationProvider);
        this.organizationName = organizationName;
    }

    @Override
    public String getEncodedAuthorization() throws IOException {
        synchronized (this) {
            if (latestToken == null || Instant.now().isAfter(this.validUntil)) {
                refreshToken();
            }
            return String.format("token %s", latestToken);
        }
    }

    private void refreshToken() throws IOException {
        GitHub gitHub = this.gitHub();
        GHAppInstallation installationByOrganization = gitHub.getApp()
                .getInstallationByOrganization(this.organizationName);
        GHAppInstallationToken ghAppInstallationToken = installationByOrganization.createToken().create();
        this.validUntil = ghAppInstallationToken.getExpiresAt().toInstant().minus(Duration.ofMinutes(5));
        this.latestToken = Objects.requireNonNull(ghAppInstallationToken.getToken());
    }
}
