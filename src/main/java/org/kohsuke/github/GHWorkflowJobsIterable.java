package org.kohsuke.github;

// TODO: Auto-generated Javadoc
/**
 * Iterable for workflow run jobs listing.
 */
class GHWorkflowJobsIterable extends PagedIterable<GHWorkflowJob> {
    private GHWorkflowJobsPage result;

    /**
     * Instantiates a new GH workflow jobs iterable.
     *
     * @param repo
     *            the repo
     * @param request
     *            the request
     */
    public GHWorkflowJobsIterable(GHRepository repo, GitHubRequest request) {
        super(new PaginatedEndpoint<>(repo.root()
                .getClient(), request, GHWorkflowJobsPage.class, GHWorkflowJob.class, item -> item.wrapUp(repo)));
    }
}
