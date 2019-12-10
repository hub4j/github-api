package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A Github Marketplace Account Plan.
 *
 * @author Paulo Miguel Almeida
 * @see GHMarketplaceListAccountBuilder#retrieve()
 */
public class GHMarketplaceAccountPlan extends GHMarketplaceAccount {

    @JsonProperty("marketplace_pending_change")
    private GHMarketplacePendingChange marketplacePendingChange;
    @JsonProperty("marketplace_purchase")
    private GHMarketplacePurchase marketplacePurchase;

    /**
     * Wrap up gh marketplace account.
     *
     * @param root
     *            the root
     * @return an instance of the GHMarketplaceAccount class
     */
    GHMarketplaceAccountPlan wrapUp(GitHub root) {
        super.wrapUp(root);
        if (this.marketplacePendingChange != null)
            this.marketplacePendingChange.wrapUp(this.root);

        if (this.marketplacePurchase != null)
            this.marketplacePurchase.wrapUp(this.root);

        return this;
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
