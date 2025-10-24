package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Date;

// TODO: Auto-generated Javadoc
/**
 * A Github Marketplace purchase.
 *
 * @author Paulo Miguel Almeida
 * @see GHMarketplaceListAccountBuilder#createRequest() GHMarketplaceListAccountBuilder#createRequest()
 */
public class GHMarketplacePurchase extends GitHubInteractiveObject {

    private String billingCycle;

    private String freeTrialEndsOn;
    private String nextBillingDate;
    private boolean onFreeTrial;
    @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Field comes from JSON deserialization")
    private GHMarketplacePlan plan;
    private Long unitCount;
    private String updatedAt;
    /**
     * Create default GHMarketplacePurchase instance
     */
    public GHMarketplacePurchase() {
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
     * Gets free trial ends on.
     *
     * @return the free trial ends on
     */
    public Date getFreeTrialEndsOn() {
        return GitHubClient.parseDate(freeTrialEndsOn);
    }

    /**
     * Gets next billing date.
     *
     * @return the next billing date
     */
    public Date getNextBillingDate() {
        return GitHubClient.parseDate(nextBillingDate);
    }

    /**
     * Gets plan.
     *
     * @return the plan
     */
    public GHMarketplacePlan getPlan() {
        return plan;
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
        return GitHubClient.parseDate(updatedAt);
    }

    /**
     * Is on free trial boolean.
     *
     * @return the boolean
     */
    public boolean isOnFreeTrial() {
        return onFreeTrial;
    }
}
