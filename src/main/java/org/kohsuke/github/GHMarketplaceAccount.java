package org.kohsuke.github;

import java.net.URL;

/**
 * Base class for Github Marketplace Account.
 *
 * @author Paulo Miguel Almeida
 * @see GitHub#getMyMarketplacePurchases()
 * @see GHMarketplaceListAccountBuilder#createRequest()
 */
public class GHMarketplaceAccount extends GitHubInteractiveObject {
    private String url;
    private long id;
    private String login;
    private String email;
    private String organizationBillingEmail;
    private GHMarketplaceAccountType type;

    /**
     * Gets url.
     *
     * @return the url
     */
    public URL getUrl() {
        return GitHubClient.parseURL(url);
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * Gets login.
     *
     * @return the login
     */
    public String getLogin() {
        return login;
    }

    /**
     * Gets email.
     *
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Gets organization billing email.
     *
     * @return the organization billing email
     */
    public String getOrganizationBillingEmail() {
        return organizationBillingEmail;
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    public GHMarketplaceAccountType getType() {
        return type;
    }

}
