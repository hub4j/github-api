package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * A Github Marketplace plan.
 *
 * @author Paulo Miguel Almeida
 * @see GitHub#listMarketplacePlans()
 */
public class GHMarketplacePlan {
    private String url;
    @JsonProperty("accounts_url")
    private String accountsUrl;
    private long id;
    private long number;
    private String name;
    private String description;
    @JsonProperty("monthly_price_in_cents")
    private long monthlyPriceInCents;
    @JsonProperty("yearly_price_in_cents")
    private long yearlyPriceInCents;
    @JsonProperty("price_model")
    private GHMarketplacePriceModel priceModel;
    @JsonProperty("has_free_trial")
    private boolean freeTrial;
    @JsonProperty("unit_name")
    private String unitName;
    private String state;
    private List<String> bullets;

    /**
     * Gets url.
     *
     * @return the url
     */
    public String getUrl() {
        return url;
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
        return bullets;
    }
}
