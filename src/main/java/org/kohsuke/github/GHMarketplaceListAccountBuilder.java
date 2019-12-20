package org.kohsuke.github;

import java.io.IOException;

/**
 * Returns any accounts associated with a plan, including free plans
 *
 * @author Paulo Miguel Almeida
 * @see GHMarketplacePlan#listAccounts()
 */
public class GHMarketplaceListAccountBuilder {
    private final GitHub root;
    private final Requester builder;
    private final long planId;

    GHMarketplaceListAccountBuilder(GitHub root, long planId) {
        this.root = root;
        this.builder = root.createRequest();
        this.planId = planId;
    }

    /**
     * Sorts the GitHub accounts by the date they were created or last updated. Can be one of created or updated.
     * <p>
     * If omitted, the default sorting strategy will be "CREATED"
     *
     * @param sort
     *            the sort strategy
     * @return a GHMarketplaceListAccountBuilder
     */
    public GHMarketplaceListAccountBuilder sort(Sort sort) {
        this.builder.with("sort", sort);
        return this;
    }

    /**
     * Orders the GitHub accounts results, Can be one of asc or desc. Ignored without the sort parameter.
     *
     * @param direction
     *            the order strategy
     * @return a GHMarketplaceListAccountBuilder
     */
    public GHMarketplaceListAccountBuilder direction(GHDirection direction) {
        this.builder.with("direction", direction);
        return this;
    }

    /**
     * The enum Sort.
     */
    public enum Sort {
        CREATED, UPDATED
    }

    /**
     * List any accounts associated with the plan specified on construction with all the order/sort parameters set.
     * <p>
     * GitHub Apps must use a JWT to access this endpoint.
     * <p>
     * OAuth Apps must use basic authentication with their client ID and client secret to access this endpoint.
     *
     * @return a paged iterable instance of GHMarketplaceAccountPlan
     * @throws IOException
     *             on error
     */
    public PagedIterable<GHMarketplaceAccountPlan> createRequest() throws IOException {
        return builder.asPagedIterable(String.format("/marketplace_listing/plans/%d/accounts", this.planId),
                GHMarketplaceAccountPlan[].class,
                item -> item.wrapUp(root));
    }

}
