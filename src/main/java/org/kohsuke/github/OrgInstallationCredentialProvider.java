package org.kohsuke.github;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * Provides a CredentialProvider that performs automatic token refresh.
 */
public class OrgInstallationCredentialProvider implements CredentialProvider {

    private GitHub baseGitHub;
    private GitHub gitHub;

    private final CredentialProvider refreshProvider;
    private final String organizationName;

    private String latestToken;

    @Nonnull
    private Instant validUntil = Instant.MIN;

    /**
     * Provides a CredentialProvider that performs automatic token refresh, based on an previously authenticated github
     * client.
     *
     * @param organizationName
     *            The name of the organization where the application is installed
     * @param credentialProvider
     *            A credential provider that returns a JWT token that can be used to refresh the App Installation token
     *            from GitHub.
     */
    @BetaApi
    @Deprecated
    public OrgInstallationCredentialProvider(String organizationName, CredentialProvider credentialProvider) {
        this.organizationName = organizationName;
        this.refreshProvider = credentialProvider;
    }

    @Override
    public void bind(GitHub github) {
        this.baseGitHub = github;
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
        if (gitHub == null) {
            gitHub = new GitHub.CredentialRefreshGitHubWrapper(this.baseGitHub, refreshProvider);
        }

        GHAppInstallation installationByOrganization = gitHub.getApp()
                .getInstallationByOrganization(this.organizationName);
        GHAppInstallationToken ghAppInstallationToken = installationByOrganization.createToken().create();
        this.validUntil = ghAppInstallationToken.getExpiresAt().toInstant().minus(Duration.ofMinutes(5));
        this.latestToken = Objects.requireNonNull(ghAppInstallationToken.getToken());
    }
}
