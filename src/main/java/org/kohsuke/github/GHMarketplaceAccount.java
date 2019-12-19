package org.kohsuke.github;

import java.net.URL;

/**
 * A Github Marketplace Account.
 *
 * @author Paulo Miguel Almeida
 * @see GHMarketplaceListAccountBuilder#createRequest()
 */
public class GHMarketplaceAccount {

    private GitHub root;
    private String url;
    private long id;
    private String login;
    private String email;
    private String organizationBillingEmail;
    private GHMarketplaceAccountType type;
    private GHMarketplacePendingChange marketplacePendingChange;
    private GHMarketplacePurchase marketplacePurchase;

    /**
     * Wrap up gh marketplace account.
     *
     * @param root
     *            the root
     * @return an instance of the GHMarketplaceAccount class
     */
    GHMarketplaceAccount wrapUp(GitHub root) {
        this.root = root;
        if (this.marketplacePendingChange != null)
            this.marketplacePendingChange.wrapUp(this.root);

        if (this.marketplacePurchase != null)
            this.marketplacePurchase.wrapUp(this.root);

        return this;
    }

    /**
     * Gets url.
     *
     * @return the url
     */
    public URL getUrl() {
        return GitHub.parseURL(url);
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

    /**
     * Gets marketplace pending change.
     *
     * @return the marketplace pending change
     */
    public GHMarketplacePendingChange getMarketplacePendingChange() {
        return marketplacePendingChange;
    }

    /**
     * Gets marketplace purchase.
     *
     * @return the marketplace purchase
     */
    public GHMarketplacePurchase getMarketplacePurchase() {
        return marketplacePurchase;
    }
}
