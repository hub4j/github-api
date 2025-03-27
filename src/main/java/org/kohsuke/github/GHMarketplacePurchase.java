package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.time.Instant;

// TODO: Auto-generated Javadoc
/**
 * A Github Marketplace purchase.
 *
 * @author Paulo Miguel Almeida
 * @see GHMarketplaceListAccountBuilder#createRequest() GHMarketplaceListAccountBuilder#createRequest()
 */
public class GHMarketplacePurchase extends GitHubInteractiveObject {

    /**
     * Create default GHMarketplacePurchase instance
     */
    public GHMarketplacePurchase() {
    }

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
    public Instant getNextBillingDate() {
        return GitHubClient.parseInstant(nextBillingDate);
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
    public Instant getFreeTrialEndsOn() {
        return GitHubClient.parseInstant(freeTrialEndsOn);
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
    public Instant getUpdatedAt() {
        return GitHubClient.parseInstant(updatedAt);
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
