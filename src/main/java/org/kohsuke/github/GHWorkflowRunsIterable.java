package org.kohsuke.github;

import java.net.MalformedURLException;
import java.util.Iterator;

import javax.annotation.Nonnull;

/**
 * Iterable for workflow runs listing.
 */
class GHWorkflowRunsIterable extends PagedIterable<GHWorkflowRun> {
    private final GHRepository owner;
    private final GitHubRequest request;

    private GHWorkflowRunsPage result;

    public GHWorkflowRunsIterable(GHRepository owner, GitHubRequest.Builder<?> requestBuilder) {
        this.owner = owner;
        try {
            this.request = requestBuilder.build();
        } catch (MalformedURLException e) {
            throw new GHException("Malformed URL", e);
        }
    }

    @Nonnull
    @Override
    public PagedIterator<GHWorkflowRun> _iterator(int pageSize) {
        return new PagedIterator<>(
                adapt(GitHubPageIterator.create(owner.root().getClient(), GHWorkflowRunsPage.class, request, pageSize)),
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
                return v.getWorkflowRuns(owner);
            }
        };
    }
}
