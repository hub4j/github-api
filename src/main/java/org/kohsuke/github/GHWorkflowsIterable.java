package org.kohsuke.github;

import java.util.Iterator;

import javax.annotation.Nonnull;

// TODO: Auto-generated Javadoc
/**
 * Iterable for workflows listing.
 */
class GHWorkflowsIterable extends PagedIterable<GHWorkflow> {
    private final transient GHRepository owner;

    private GHWorkflowsPage result;

    /**
     * Instantiates a new GH workflows iterable.
     *
     * @param owner the owner
     */
    public GHWorkflowsIterable(GHRepository owner) {
        this.owner = owner;
    }

    /**
     * Iterator.
     *
     * @param pageSize the page size
     * @return the paged iterator
     */
    @Nonnull
    @Override
    public PagedIterator<GHWorkflow> _iterator(int pageSize) {
        GitHubRequest request = owner.root()
                .createRequest()
                .withUrlPath(owner.getApiTailUrl("actions/workflows"))
                .build();

        return new PagedIterator<>(
                adapt(GitHubPageIterator.create(owner.root().getClient(), GHWorkflowsPage.class, request, pageSize)),
                null);
    }

    /**
     * Adapt.
     *
     * @param base the base
     * @return the iterator
     */
    protected Iterator<GHWorkflow[]> adapt(final Iterator<GHWorkflowsPage> base) {
        return new Iterator<GHWorkflow[]>() {
            public boolean hasNext() {
                return base.hasNext();
            }

            public GHWorkflow[] next() {
                GHWorkflowsPage v = base.next();
                if (result == null) {
                    result = v;
                }
                return v.getWorkflows(owner);
            }
        };
    }
}
