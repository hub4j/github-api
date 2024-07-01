package org.kohsuke.github;

import java.util.Iterator;

import javax.annotation.Nonnull;

// TODO: Auto-generated Javadoc
/**
 * Iterable for artifacts listing.
 */
class GHArtifactsIterable extends PagedIterable<GHArtifact> {
    private final transient GHRepository owner;
    private final GitHubRequest request;

    private GHArtifactsPage result;

    /**
     * Instantiates a new GH artifacts iterable.
     *
     * @param owner
     *            the owner
     * @param requestBuilder
     *            the request builder
     */
    public GHArtifactsIterable(GHRepository owner, GitHubRequest.Builder<?> requestBuilder) {
        this.owner = owner;
        this.request = requestBuilder.build();
    }

    @Nonnull
    @Override
    public Paginator<GHArtifact> _paginator(int pageSize, int startPage) {
        return new Paginator<>(
                adapt(GitHubPaginator
                        .create(owner.root().getClient(), GHArtifactsPage.class, request, pageSize, startPage)),
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
    public PagedIterator<GHArtifact> _iterator(int pageSize) {
        return new PagedIterator<>(
                adapt(GitHubPageIterator.create(owner.root().getClient(), GHArtifactsPage.class, request, pageSize)),
                null);
    }

    /**
     * Adapt.
     *
     * @param base
     *            the base
     * @return the iterator
     */
    protected Iterator<GHArtifact[]> adapt(final Iterator<GHArtifactsPage> base) {
        return new Iterator<GHArtifact[]>() {
            public boolean hasNext() {
                return base.hasNext();
            }

            public GHArtifact[] next() {
                GHArtifactsPage v = base.next();
                if (result == null) {
                    result = v;
                }
                return v.getArtifacts(owner);
            }
        };
    }

    protected NavigablePageIterator<GHArtifact[]> adapt(final NavigablePageIterator<GHArtifactsPage> base) {
        return new NavigablePageIterator<GHArtifact[]>() {
            @Override
            public boolean hasPrevious() {
                return base.hasPrevious();
            }

            @Override
            public GHArtifact[] previous() {
                return base.previous().getArtifacts(owner);
            }

            @Override
            public GHArtifact[] first() {
                return base.first().getArtifacts(owner);
            }

            @Override
            public GHArtifact[] last() {
                return base.last().getArtifacts(owner);
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
            public GHArtifact[] next() {
                return base.next().getArtifacts(owner);
            }

            @Override
            public GHArtifact[] jumpToPage(int page) {
                return base.jumpToPage(page).getArtifacts(owner);
            }

            @Override
            public void refresh() {
                base.refresh();
            }
        };
    }
}
