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
    private String nextBillingDate;
    private boolean onFreeTrial;
    private String freeTrialEndsOn;
    private Long unitCount;
    private String updatedAt;
    @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Field comes from JSON deserialization")
    private GHMarketplacePlan plan;

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
        return GitHubClient.parseDate(nextBillingDate);
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
        return GitHubClient.parseDate(freeTrialEndsOn);
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
     * Gets plan.
     *
     * @return the plan
     */
    public GHMarketplacePlan getPlan() {
        return plan;
    }
}
