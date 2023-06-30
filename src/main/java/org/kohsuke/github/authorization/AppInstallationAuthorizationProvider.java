package org.kohsuke.github.authorization;

import org.kohsuke.github.BetaApi;
import org.kohsuke.github.GHApp;
import org.kohsuke.github.GHAppInstallation;
import org.kohsuke.github.GHAppInstallationToken;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * An AuthorizationProvider that performs automatic token refresh for an organization's AppInstallation.
 */
public class AppInstallationAuthorizationProvider extends GitHub.DependentAuthorizationProvider {

    private final AppInstallationProvider appInstallationProvider;

    private String authorization;

    @Nonnull
    private Instant validUntil = Instant.MIN;

    /**
     * Provides an AuthorizationProvider that performs automatic token refresh, based on an previously authenticated
     * github client.
     *
     * @param appInstallationProvider
     *            An AppInstallationProvider that the authorization provider will use to retrieve the App.
     * @param authorizationProvider
     *            A authorization provider that returns a JWT token that can be used to refresh the App Installation
     *            token from GitHub.
     */
    @BetaApi
    public AppInstallationAuthorizationProvider(AppInstallationProvider appInstallationProvider,
            AuthorizationProvider authorizationProvider) {
        super(authorizationProvider);
        this.appInstallationProvider = appInstallationProvider;
    }

    @Override
    public String getEncodedAuthorization() throws IOException {
        synchronized (this) {
            if (authorization == null || Instant.now().isAfter(this.validUntil)) {
                String token = refreshToken();
                authorization = String.format("token %s", token);
            }
            return authorization;
        }
    }

    private String refreshToken() throws IOException {
        GitHub gitHub = this.gitHub();
        GHAppInstallation installationByOrganization = appInstallationProvider.getAppInstallation(gitHub.getApp());
        GHAppInstallationToken ghAppInstallationToken = installationByOrganization.createToken().create();
        this.validUntil = ghAppInstallationToken.getExpiresAt().toInstant().minus(Duration.ofMinutes(5));
        return Objects.requireNonNull(ghAppInstallationToken.getToken());
    }

    /**
     * Provides an interface that returns an app to be used by an AppInstallationAuthorizationProvider
     */
    @FunctionalInterface
    public interface AppInstallationProvider {
        /**
         * Provides a GHAppInstallation for the given GHApp
         *
         * @param app
         *            The GHApp to use
         * @return The GHAppInstallation
         * @throws IOException
         *             on error
         */
        GHAppInstallation getAppInstallation(GHApp app) throws IOException;
    }
}
