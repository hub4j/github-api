package org.kohsuke.github;

import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.time.Instant;
import java.util.Date;

// TODO: Auto-generated Javadoc
/**
 * Github Marketplace User Purchase.
 *
 * @author Paulo Miguel Almeida
 * @see GitHub#getMyMarketplacePurchases()
 */
public class GHMarketplaceUserPurchase extends GitHubInteractiveObject {

    @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Field comes from JSON deserialization")
    private GHMarketplaceAccount account;

    private String billingCycle;
    private String freeTrialEndsOn;
    private String nextBillingDate;
    private boolean onFreeTrial;
    @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Field comes from JSON deserialization")
    private GHMarketplacePlan plan;
    private Long unitCount;
    private String updatedAt;
    /**
     * Create default GHMarketplaceUserPurchase instance
     */
    public GHMarketplaceUserPurchase() {
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
    @WithBridgeMethods(value = Date.class, adapterMethod = "instantToDate")
    public Instant getFreeTrialEndsOn() {
        return GitHubClient.parseInstant(freeTrialEndsOn);
    }

    /**
     * Gets next billing date.
     *
     * @return the next billing date
     */
    @WithBridgeMethods(value = Date.class, adapterMethod = "instantToDate")
    public Instant getNextBillingDate() {
        return GitHubClient.parseInstant(nextBillingDate);
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
    @WithBridgeMethods(value = Date.class, adapterMethod = "instantToDate")
    public Instant getUpdatedAt() {
        return GitHubClient.parseInstant(updatedAt);
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
