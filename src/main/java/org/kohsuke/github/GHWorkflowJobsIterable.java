package org.kohsuke.github;

import java.util.Iterator;

import javax.annotation.Nonnull;

/**
 * Iterable for workflow run jobs listing.
 */
class GHWorkflowJobsIterable extends PagedIterable<GHWorkflowJob> {
    private final GHRepository repo;
    private final GitHubRequest request;

    private GHWorkflowJobsPage result;

    public GHWorkflowJobsIterable(GHRepository repo, GitHubRequest request) {
        this.repo = repo;
        this.request = request;
    }

    @Nonnull
    @Override
    public PagedIterator<GHWorkflowJob> _iterator(int pageSize) {
        return new PagedIterator<>(
                adapt(GitHubPageIterator.create(repo.root.getClient(), GHWorkflowJobsPage.class, request, pageSize)),
                null);
    }

    protected Iterator<GHWorkflowJob[]> adapt(final Iterator<GHWorkflowJobsPage> base) {
        return new Iterator<GHWorkflowJob[]>() {
            public boolean hasNext() {
                return base.hasNext();
            }

            public GHWorkflowJob[] next() {
                GHWorkflowJobsPage v = base.next();
                if (result == null) {
                    result = v;
                }
                return v.getWorkflowJobs(repo);
            }
        };
    }
}
