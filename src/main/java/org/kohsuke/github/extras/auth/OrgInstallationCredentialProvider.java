package org.kohsuke.github.extras.auth;

import org.kohsuke.github.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;

/**
 * This helper class provides an example on how to authenticate a GitHub instance with an installation token, that will
 * be automatically refreshed when required.
 */
public class OrgInstallationCredentialProvider implements CredentialProvider {

    private final GitHub gitHub;

    private final String organizationName;

    private String latestToken;

    private Date validUntil;

    public OrgInstallationCredentialProvider(String organizationName, GitHub gitHub) {
        this.organizationName = organizationName;
        this.gitHub = gitHub;
    }
    /**
     * Obtains a new OAuth2 token, using the configured client to request it. The configured client <b>must</b> be able
     * to request the token, this usually means that it needs to have JWT authentication
     * 
     * @throws IOException
     *             for any problem obtaining the token
     */
    @Preview
    @Override
    @Deprecated
    public String getEncodedAuthorization() throws IOException {
        if (this.latestToken == null || this.validUntil == null || (new Date()).after(this.validUntil)) {
            this.refreshToken();
        }

        return String.format("token %s", this.latestToken);
    }

    @Preview
    @Deprecated
    private void refreshToken() throws IOException {
        GHAppInstallation installationByOrganization = this.gitHub.getApp()
                .getInstallationByOrganization(this.organizationName);
        GHAppInstallationToken ghAppInstallationToken = installationByOrganization.createToken().create();
        this.validUntil = ghAppInstallationToken.getExpiresAt();
        this.latestToken = ghAppInstallationToken.getToken();
    }

    public static GitHub getAuthenticatedClient()
            throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {
        // Build a client that will be used to get Oauth tokens with a JWT token
        GitHub jwtAuthenticatedClient = new GitHubBuilder()
                .withCredentialProvider(new JWTTokenProvider("12345", Paths.get("~/github-api-app.private-key.der")))
                .build();
        // Build another client (the final one) that will use the Oauth token, and automatically refresh it when
        // it is expired. This is the client that can either be further customized, or used directly.
        return new GitHubBuilder()
                .withCredentialProvider(new OrgInstallationCredentialProvider("myOrganization", jwtAuthenticatedClient))
                .build();
    }

}
