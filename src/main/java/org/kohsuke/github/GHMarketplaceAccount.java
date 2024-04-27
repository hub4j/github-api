package org.kohsuke.github;

import java.io.IOException;
import java.net.URL;

// TODO: Auto-generated Javadoc
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

    /**
     * Shows whether the user or organization account actively subscribes to a plan listed by the authenticated GitHub
     * App. When someone submits a plan change that won't be processed until the end of their billing cycle, you will
     * also see the upcoming pending change.
     *
     * <p>
     * GitHub Apps must use a JWT to access this endpoint.
     * <p>
     * OAuth Apps must use basic authentication with their client ID and client secret to access this endpoint.
     *
     * @return a GHMarketplaceListAccountBuilder instance
     * @throws IOException
     *             in case of {@link IOException}
     * @see <a href=
     *      "https://docs.github.com/en/rest/apps/marketplace?apiVersion=2022-11-28#get-a-subscription-plan-for-an-account">Get
     *      a subscription plan for an account</a>
     */
    public GHMarketplaceAccountPlan getPlan() throws IOException {
        return new GHMarketplacePlanForAccountBuilder(root(), this.id).createRequest();
    }

}
