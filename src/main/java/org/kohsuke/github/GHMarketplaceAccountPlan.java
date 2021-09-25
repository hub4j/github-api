package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * A Github Marketplace Account Plan.
 *
 * @author Paulo Miguel Almeida
 * @see GHMarketplaceListAccountBuilder#createRequest()
 */
public class GHMarketplaceAccountPlan extends GHMarketplaceAccount {

    @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Field comes from JSON deserialization")
    private GHMarketplacePendingChange marketplacePendingChange;
    @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Field comes from JSON deserialization")
    private GHMarketplacePurchase marketplacePurchase;

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
