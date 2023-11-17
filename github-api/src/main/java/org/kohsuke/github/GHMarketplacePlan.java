package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URL;
import java.util.Collections;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * A Github Marketplace plan.
 *
 * @author Paulo Miguel Almeida
 * @see GitHub#listMarketplacePlans()
 */
public class GHMarketplacePlan extends GitHubInteractiveObject {
    private String url;
    private String accountsUrl;
    private long id;
    private long number;
    private String name;
    private String description;
    private long monthlyPriceInCents;
    private long yearlyPriceInCents;
    private GHMarketplacePriceModel priceModel;
    @JsonProperty("has_free_trial")
    private boolean freeTrial; // JavaBeans Spec 1.01 section 8.3.2 forces us to have is<propertyName>
    private String unitName;
    private String state;
    private List<String> bullets;

    /**
     * Gets url.
     *
     * @return the url
     */
    public URL getUrl() {
        return GitHubClient.parseURL(url);
    }

    /**
     * Gets accounts url.
     *
     * @return the accounts url
     */
    public String getAccountsUrl() {
        return accountsUrl;
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
     * Gets number.
     *
     * @return the number
     */
    public long getNumber() {
        return number;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets monthly price in cents.
     *
     * @return the monthly price in cents
     */
    public long getMonthlyPriceInCents() {
        return monthlyPriceInCents;
    }

    /**
     * Gets yearly price in cents.
     *
     * @return the yearly price in cents
     */
    public long getYearlyPriceInCents() {
        return yearlyPriceInCents;
    }

    /**
     * Gets price model.
     *
     * @return the price model
     */
    public GHMarketplacePriceModel getPriceModel() {
        return priceModel;
    }

    /**
     * Is free trial boolean.
     *
     * @return the boolean
     */
    public boolean isFreeTrial() {
        return freeTrial;
    }

    /**
     * Gets unit name.
     *
     * @return the unit name
     */
    public String getUnitName() {
        return unitName;
    }

    /**
     * Gets state.
     *
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * Gets bullets.
     *
     * @return the bullets
     */
    public List<String> getBullets() {
        return Collections.unmodifiableList(bullets);
    }

    /**
     * Starts a builder that list any accounts associated with a plan, including free plans. For per-seat pricing, you
     * see the list of accounts that have purchased the plan, including the number of seats purchased. When someone
     * submits a plan change that won't be processed until the end of their billing cycle, you will also see the
     * upcoming pending change.
     *
     * <p>
     * You use the returned builder to set various properties, then call
     * {@link GHMarketplaceListAccountBuilder#createRequest()} to finally list the accounts related to this plan.
     *
     * <p>
     * GitHub Apps must use a JWT to access this endpoint.
     * <p>
     * OAuth Apps must use basic authentication with their client ID and client secret to access this endpoint.
     *
     * @return a GHMarketplaceListAccountBuilder instance
     * @see <a href=
     *      "https://developer.github.com/v3/apps/marketplace/#list-all-github-accounts-user-or-organization-on-a-specific-plan">List
     *      all GitHub accounts (user or organization) on a specific plan</a>
     */
    public GHMarketplaceListAccountBuilder listAccounts() {
        return new GHMarketplaceListAccountBuilder(root(), this.id);
    }
}
