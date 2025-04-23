package org.kohsuke.github;

// TODO: Auto-generated Javadoc
/**
 * Iterable for workflow runs listing.
 */
class GHWorkflowRunsIterable extends PagedIterable<GHWorkflowRun> {
    /**
     * Instantiates a new GH workflow runs iterable.
     *
     * @param owner
     *            the owner
     * @param requestBuilder
     *            the request builder
     */
    public GHWorkflowRunsIterable(GHRepository owner, GitHubRequest.Builder<?> requestBuilder) {
        super(new GitHubEndpointIterable<>(owner.root().getClient(),
                requestBuilder.build(),
                GHWorkflowRunsPage.class,
                GHWorkflowRun.class,
                item -> item.wrapUp(owner)));
    }
}
