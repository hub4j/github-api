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
     * @param owner
     *            the owner
     */
    public GHWorkflowsIterable(GHRepository owner) {
        this.owner = owner;
    }

    @Nonnull
    @Override
    public Paginator<GHWorkflow> _paginator(int pageSize, int startPage) {
        GitHubRequest request = owner.root()
                .createRequest()
                .withUrlPath(owner.getApiTailUrl("actions/workflows"))
                .build();
        return new Paginator<>(
                adapt(GitHubPaginator
                        .create(owner.root().getClient(), GHWorkflowsPage.class, request, pageSize, startPage)),
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
     * @param base
     *            the base
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

    protected NavigablePageIterator<GHWorkflow[]> adapt(final NavigablePageIterator<GHWorkflowsPage> base) {
        return new NavigablePageIterator<GHWorkflow[]>() {
            @Override
            public boolean hasPrevious() {
                return base.hasPrevious();
            }

            @Override
            public GHWorkflow[] previous() {
                return base.previous().getWorkflows(owner);
            }

            @Override
            public GHWorkflow[] first() {
                return base.first().getWorkflows(owner);
            }

            @Override
            public GHWorkflow[] last() {
                return base.last().getWorkflows(owner);
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
            public GHWorkflow[] next() {
                return base.next().getWorkflows(owner);
            }

            @Override
            public GHWorkflow[] jumpToPage(int page) {
                return base.jumpToPage(page).getWorkflows(owner);
            }

            @Override
            public void refresh() {
                base.refresh();
            }
        };
    }
}
