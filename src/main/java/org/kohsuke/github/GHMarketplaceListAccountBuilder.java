package org.kohsuke.github;

import java.io.IOException;

// TODO: Auto-generated Javadoc
/**
 * Returns any accounts associated with a plan, including free plans.
 *
 * @author Paulo Miguel Almeida
 * @see GHMarketplacePlan#listAccounts()
 */
public class GHMarketplaceListAccountBuilder extends GitHubInteractiveObject {
    private final Requester builder;
    private final long planId;

    /**
     * Instantiates a new GH marketplace list account builder.
     *
     * @param root the root
     * @param planId the plan id
     */
    GHMarketplaceListAccountBuilder(GitHub root, long planId) {
        super(root);
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
        
        /** The created. */
        CREATED, 
 /** The updated. */
 UPDATED
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
        return builder.withUrlPath(String.format("/marketplace_listing/plans/%d/accounts", this.planId))
                .toIterable(GHMarketplaceAccountPlan[].class, null);
    }

}
