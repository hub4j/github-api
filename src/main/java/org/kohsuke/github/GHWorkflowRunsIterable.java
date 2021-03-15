package org.kohsuke.github;

import java.util.Iterator;

import javax.annotation.Nonnull;

/**
 * Iterable for workflow runs listing.
 */
class GHWorkflowRunsIterable extends PagedIterable<GHWorkflowRun> {
    private final transient GitHub root;
    private final GitHubRequest request;

    private GHWorkflowRunsPage result;

    public GHWorkflowRunsIterable(GitHub root, GitHubRequest request) {
        this.root = root;
        this.request = request;
    }

    @Nonnull
    @Override
    public PagedIterator<GHWorkflowRun> _iterator(int pageSize) {
        return new PagedIterator<>(
                adapt(GitHubPageIterator.create(root.getClient(), GHWorkflowRunsPage.class, request, pageSize)),
                null);
    }

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
                return v.getWorkflowRuns(root);
            }
        };
    }
}
