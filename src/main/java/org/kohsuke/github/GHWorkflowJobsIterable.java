package org.kohsuke.github;

import java.util.Iterator;

import javax.annotation.Nonnull;

// TODO: Auto-generated Javadoc
/**
 * Iterable for workflow run jobs listing.
 */
class GHWorkflowJobsIterable extends PagedIterable<GHWorkflowJob> {
    private final GHRepository repo;
    private final GitHubRequest request;

    private GHWorkflowJobsPage result;

    /**
     * Instantiates a new GH workflow jobs iterable.
     *
     * @param repo the repo
     * @param request the request
     */
    public GHWorkflowJobsIterable(GHRepository repo, GitHubRequest request) {
        this.repo = repo;
        this.request = request;
    }

    /**
     * Iterator.
     *
     * @param pageSize the page size
     * @return the paged iterator
     */
    @Nonnull
    @Override
    public PagedIterator<GHWorkflowJob> _iterator(int pageSize) {
        return new PagedIterator<>(
                adapt(GitHubPageIterator.create(repo.root().getClient(), GHWorkflowJobsPage.class, request, pageSize)),
                null);
    }

    /**
     * Adapt.
     *
     * @param base the base
     * @return the iterator
     */
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
