package org.kohsuke.github;

import java.io.IOException;
import java.util.Date;

/**
 * Provides a CredentialProvider that performs automatic token refresh based on a {@link JWTTokenProvider} that always
 * returns a valid and up-to-date JWT Token.
 */
public class OrgInstallationCredentialProvider implements CredentialProvider {

    private final String organizationName;
    private final JWTTokenProvider jwtTokenProvider;

    private String latestToken;
    private Date validUntil;

    /**
     * Provides a CredentialProvider that performs automatic token refresh based on a {@link JWTTokenProvider} that
     * always returns a valid and up-to-date JWT Token.
     *
     * @param organizationName The name of the organization where the application is installed
     * @param jwtTokenProvider A {@link JWTTokenProvider} that always returns valid and up-to-date JWT Tokens for the given
     *                         application.
     */
    public OrgInstallationCredentialProvider(String organizationName, JWTTokenProvider jwtTokenProvider) {
        this.organizationName = organizationName;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public String getEncodedAuthorization() throws IOException {
        if (latestToken == null || validUntil == null || new Date().compareTo(this.validUntil) > 0) {
            refreshToken();
        }
        return latestToken;
    }

    private void refreshToken() throws IOException {
        GitHub gh = new GitHubBuilder().withJwtToken(jwtTokenProvider.token()).build();
        GHAppInstallation installationByOrganization = gh.getApp().getInstallationByOrganization(this.organizationName);
        GHAppInstallationToken ghAppInstallationToken = installationByOrganization.createToken().create();
        this.validUntil = ghAppInstallationToken.getExpiresAt();
        this.latestToken = ghAppInstallationToken.getToken();
    }
}
