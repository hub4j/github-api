package org.kohsuke.github;

// TODO: Auto-generated Javadoc
/**
 * Iterable for check-runs listing.
 */
class GHCheckRunsIterable extends PagedIterable<GHCheckRun> {
    /**
     * Instantiates a new GH check runs iterable.
     *
     * @param owner
     *            the owner
     * @param request
     *            the request
     */
    public GHCheckRunsIterable(GHRepository owner, GitHubRequest request) {
        super(new PaginatedEndpoint<>(owner.root()
                .getClient(), request, GHCheckRunsPage.class, GHCheckRun.class, item -> item.wrap(owner)));
    }
}
