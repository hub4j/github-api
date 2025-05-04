package org.kohsuke.github;

// TODO: Auto-generated Javadoc
/**
 * Iterable for workflows listing.
 */
class GHWorkflowsIterable extends PagedIterable<GHWorkflow> {

    /**
     * Instantiates a new GH workflows iterable.
     *
     * @param owner
     *            the owner
     */
    public GHWorkflowsIterable(GHRepository owner) {
        super(new PaginatedEndpoint<>(owner.root().getClient(),
                owner.root().createRequest().withUrlPath(owner.getApiTailUrl("actions/workflows")).build(),
                GHWorkflowsPage.class,
                GHWorkflow.class,
                item -> item.wrapUp(owner)));
    }
}
