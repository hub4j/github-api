package org.kohsuke.github;

import java.util.Iterator;

import javax.annotation.Nonnull;

// TODO: Auto-generated Javadoc
/**
 * Iterable for workflow runs listing.
 */
class GHWorkflowRunsIterable extends PagedIterable<GHWorkflowRun> {
    private final GHRepository owner;
    private final GitHubRequest request;

    private GHWorkflowRunsPage result;

    /**
     * Instantiates a new GH workflow runs iterable.
     *
     * @param owner the owner
     * @param requestBuilder the request builder
     */
    public GHWorkflowRunsIterable(GHRepository owner, GitHubRequest.Builder<?> requestBuilder) {
        this.owner = owner;
        this.request = requestBuilder.build();
    }

    /**
     * Iterator.
     *
     * @param pageSize the page size
     * @return the paged iterator
     */
    @Nonnull
    @Override
    public PagedIterator<GHWorkflowRun> _iterator(int pageSize) {
        return new PagedIterator<>(
                adapt(GitHubPageIterator.create(owner.root().getClient(), GHWorkflowRunsPage.class, request, pageSize)),
                null);
    }

    /**
     * Adapt.
     *
     * @param base the base
     * @return the iterator
     */
    protected Iterator<GHWorkflowRun[]> adapt(final Iterator<GHWorkflowRunsPage> base) {
        return new Iterator<GHWorkflowRun[]>() {
            public boolean hasNext() {
                return base.hasNext();
            }

            public GHWorkflowRun[] next() {
                GHWorkflowRunsPage v = base.next();
                if (result == null) {
                    result = v;
                }
                return v.getWorkflowRuns(owner);
            }
        };
    }
}
