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
     * @param repo
     *            the repo
     * @param request
     *            the request
     */
    public GHWorkflowJobsIterable(GHRepository repo, GitHubRequest request) {
        this.repo = repo;
        this.request = request;
    }

    @Nonnull
    @Override
    public Paginator<GHWorkflowJob> _paginator(int pageSize, int startPage) {
        return new Paginator<>(
                adapt(GitHubPaginator
                        .create(repo.root().getClient(), GHWorkflowJobsPage.class, request, pageSize, startPage)),
                null);
    }

    /**
     * Iterator.
     *
     * @param pageSize
     *            the page size
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
     * @param base
     *            the base
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

    protected NavigablePageIterator<GHWorkflowJob[]> adapt(final NavigablePageIterator<GHWorkflowJobsPage> base) {
        return new NavigablePageIterator<GHWorkflowJob[]>() {
            @Override
            public boolean hasPrevious() {
                return base.hasPrevious();
            }

            @Override
            public GHWorkflowJob[] previous() {
                return base.previous().getWorkflowJobs(repo);
            }

            @Override
            public GHWorkflowJob[] first() {
                return base.first().getWorkflowJobs(repo);
            }

            @Override
            public GHWorkflowJob[] last() {
                return base.last().getWorkflowJobs(repo);
            }

            @Override
            public int totalCount() {
                return base.totalCount();
            }

            @Override
            public int currentPage() {
                return base.currentPage();
            }

            @Override
            public boolean hasNext() {
                return base.hasNext();
            }

            @Override
            public GHWorkflowJob[] next() {
                return base.next().getWorkflowJobs(repo);
            }

            @Override
            public GHWorkflowJob[] jumpToPage(int page) {
                return base.jumpToPage(page).getWorkflowJobs(repo);
            }

            @Override
            public void refresh() {
                base.refresh();
            }
        };
    }
}
