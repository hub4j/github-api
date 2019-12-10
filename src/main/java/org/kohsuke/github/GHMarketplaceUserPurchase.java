package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Date;

/**
 * Github Marketplace User Purchase
 *
 * @author Paulo Miguel Almeida
 * @see GitHub#getMyMarketplacePurchases()
 */
public class GHMarketplaceUserPurchase {
    protected GitHub root;
    @JsonProperty("billing_cycle")
    private String billingCycle;
    @JsonProperty("next_billing_date")
    private String nextBillingDate;
    @JsonProperty("on_free_trial")
    private boolean onFreeTrial;
    @JsonProperty("free_trial_ends_on")
    private String freeTrialEndsOn;
    @JsonProperty("unit_count")
    private Long unitCount;
    @JsonProperty("updated_at")
    private String updatedAt;
    @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Field comes from JSON deserialization")
    private GHMarketplaceAccount account;
    @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Field comes from JSON deserialization")
    private GHMarketplacePlan plan;

    /**
     * Wrap up GHMarketplaceUserPurchase.
     *
     * @param root
     *            the root
     * @return an instance of the GHMarketplaceUserPurchase class
     */
    GHMarketplaceUserPurchase wrapUp(GitHub root) {
        this.root = root;
        if (this.account != null)
            this.account.wrapUp(this.root);
        if (this.plan != null)
            this.plan.wrapUp(this.root);
        return this;
    }

    /**
     * Gets billing cycle.
     *
     * @return the billing cycle
     */
    public String getBillingCycle() {
        return billingCycle;
    }

    /**
     * Gets next billing date.
     *
     * @return the next billing date
     */
    public Date getNextBillingDate() {
        return GitHub.parseDate(nextBillingDate);
    }

    /**
     * Is on free trial boolean.
     *
     * @return the boolean
     */
    public boolean isOnFreeTrial() {
        return onFreeTrial;
    }

    /**
     * Gets free trial ends on.
     *
     * @return the free trial ends on
     */
    public Date getFreeTrialEndsOn() {
        return GitHub.parseDate(freeTrialEndsOn);
    }

    /**
     * Gets unit count.
     *
     * @return the unit count
     */
    public Long getUnitCount() {
        return unitCount;
    }

    /**
     * Gets updated at.
     *
     * @return the updated at
     */
    public Date getUpdatedAt() {
        return GitHub.parseDate(updatedAt);
    }

    /**
     * Gets account.
     *
     * @return the account
     */
    public GHMarketplaceAccount getAccount() {
        return account;
    }

    /**
     * Gets plan.
     *
     * @return the plan
     */
    public GHMarketplacePlan getPlan() {
        return plan;
    }
}
