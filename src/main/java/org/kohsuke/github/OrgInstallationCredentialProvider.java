package org.kohsuke.github;

import java.io.IOException;
import java.util.Date;

/**
 * Provides a CredentialProvider that performs automatic token refresh based on a {@link JWTTokenProvider} that always
 * returns a valid and up-to-date JWT Token.
 */
public class OrgInstallationCredentialProvider implements CredentialProvider {

    private final GitHub gitHub;

    private final String organizationName;

    private String latestToken;
    private Date validUntil;

    /**
     * Provides a CredentialProvider that performs automatic token refresh based on a {@link JWTTokenProvider} that
     * always returns a valid and up-to-date JWT Token.
     *
     * @param organizationName
     *            The name of the organization where the application is installed
     * @param gitHub
     *            A GitHub client that must be configured with a valid JWT token
     */
    public OrgInstallationCredentialProvider(String organizationName, GitHub gitHub) {
        this.organizationName = organizationName;
        this.gitHub = gitHub;
    }

    @Override
    public String getEncodedAuthorization() throws IOException {
        if (latestToken == null || validUntil == null || new Date().after(this.validUntil)) {
            refreshToken();
        }
        return String.format("token %s", latestToken);
    }

    private void refreshToken() throws IOException {
        GHAppInstallation installationByOrganization = gitHub.getApp()
                .getInstallationByOrganization(this.organizationName);
        GHAppInstallationToken ghAppInstallationToken = installationByOrganization.createToken().create();
        this.validUntil = ghAppInstallationToken.getExpiresAt();
        this.latestToken = ghAppInstallationToken.getToken();
    }
}
