package org.kohsuke.github;

import java.util.Iterator;

import javax.annotation.Nonnull;

/**
 * Iterable for workflow run jobs listing.
 */
class GHWorkflowRunJobsIterable extends PagedIterable<GHWorkflowRunJob> {
    private final GHRepository repo;
    private final GitHubRequest request;

    private GHWorkflowRunJobsPage result;

    public GHWorkflowRunJobsIterable(GHRepository repo, GitHubRequest request) {
        this.repo = repo;
        this.request = request;
    }

    @Nonnull
    @Override
    public PagedIterator<GHWorkflowRunJob> _iterator(int pageSize) {
        return new PagedIterator<>(
                adapt(GitHubPageIterator.create(repo.root.getClient(), GHWorkflowRunJobsPage.class, request, pageSize)),
                null);
    }

    protected Iterator<GHWorkflowRunJob[]> adapt(final Iterator<GHWorkflowRunJobsPage> base) {
        return new Iterator<GHWorkflowRunJob[]>() {
            public boolean hasNext() {
                return base.hasNext();
            }

            public GHWorkflowRunJob[] next() {
                GHWorkflowRunJobsPage v = base.next();
                if (result == null) {
                    result = v;
                }
                return v.getWorkflowRunJobs(repo);
            }
        };
    }
}
