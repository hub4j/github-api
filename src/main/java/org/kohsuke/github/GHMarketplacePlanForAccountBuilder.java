package org.kohsuke.github;

import java.io.IOException;

// TODO: Auto-generated Javadoc
/**
 * Returns the plan associated with current account.
 *
 * @author Benoit Lacelle
 * @see GHMarketplacePlan#listAccounts()
 * @see GitHub#listMarketplacePlans()
 */
public class GHMarketplacePlanForAccountBuilder extends GitHubInteractiveObject {
    private final Requester builder;
    private final long accountId;

    /**
     * Instantiates a new GH marketplace list account builder.
     *
     * @param root
     *            the root
     * @param accountId
     *            the account id
     */
    GHMarketplacePlanForAccountBuilder(GitHub root, long accountId) {
        super(root);
        this.builder = root.createRequest();
        this.accountId = accountId;
    }

    /**
     * Fetch the plan associated with the account specified on construction.
     * <p>
     * GitHub Apps must use a JWT to access this endpoint.
     *
     * @return a GHMarketplaceAccountPlan
     * @throws IOException
     *             on error
     */
    public GHMarketplaceAccountPlan createRequest() throws IOException {
        return builder.withUrlPath(String.format("/marketplace_listing/accounts/%d", this.accountId))
                .fetch(GHMarketplaceAccountPlan.class);
    }

}
