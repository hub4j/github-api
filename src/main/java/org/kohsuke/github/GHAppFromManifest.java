package org.kohsuke.github;

/**
 * A GitHub App with the additional attributes returned during its creation.
 *
 * @author Daniel Baur
 * @see GitHub#createAppFromManifest(String)
 */
public class GHAppFromManifest extends GHApp {

    private String clientId;

    private String clientSecret;
    private String pem;
    private String webhookSecret;
    /**
     * Create default GHAppFromManifest instance
     */
    public GHAppFromManifest() {
    }

    /**
     * Gets the client id
     *
     * @return the client id
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Gets the client secret
     *
     * @return the client secret
     */
    public String getClientSecret() {
        return clientSecret;
    }

    /**
     * Gets the pem
     *
     * @return the pem
     */
    public String getPem() {
        return pem;
    }

    /**
     * Gets the webhook secret
     *
     * @return the webhook secret
     */
    public String getWebhookSecret() {
        return webhookSecret;
    }
}
