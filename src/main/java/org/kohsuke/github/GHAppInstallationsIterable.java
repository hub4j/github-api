package org.kohsuke.github;

import java.util.Iterator;

import javax.annotation.Nonnull;

// TODO: Auto-generated Javadoc
/**
 * Iterable for GHAppInstallation listing.
 */
class GHAppInstallationsIterable extends PagedIterable<GHAppInstallation> {

    /** The Constant APP_INSTALLATIONS_URL. */
    public static final String APP_INSTALLATIONS_URL = "/user/installations";
    private final transient GitHub root;
    private GHAppInstallationsPage result;

    /**
     * Instantiates a new GH app installations iterable.
     *
     * @param root
     *            the root
     */
    public GHAppInstallationsIterable(GitHub root) {
        this.root = root;
    }

    @Nonnull
    @Override
    public Paginator<GHAppInstallation> _paginator(int pageSize, int startPage) {
        final GitHubRequest request = root.createRequest().withUrlPath(APP_INSTALLATIONS_URL).build();

        return new Paginator<>(
                adapt(GitHubPaginator
                        .create(root.getClient(), GHAppInstallationsPage.class, request, pageSize, startPage)),
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
    public PagedIterator<GHAppInstallation> _iterator(int pageSize) {
        final GitHubRequest request = root.createRequest().withUrlPath(APP_INSTALLATIONS_URL).build();
        return new PagedIterator<>(
                adapt(GitHubPageIterator.create(root.getClient(), GHAppInstallationsPage.class, request, pageSize)),
                null);
    }

    /**
     * Adapt.
     *
     * @param base
     *            the base
     * @return the iterator
     */
    protected Iterator<GHAppInstallation[]> adapt(final Iterator<GHAppInstallationsPage> base) {
        return new Iterator<GHAppInstallation[]>() {
            public boolean hasNext() {
                return base.hasNext();
            }

            public GHAppInstallation[] next() {
                GHAppInstallationsPage v = base.next();
                if (result == null) {
                    result = v;
                }
                return v.getInstallations();
            }
        };
    }

    protected NavigableIterator<GHAppInstallation[]> adapt(final NavigableIterator<GHAppInstallationsPage> base) {
        return new NavigableIterator<GHAppInstallation[]>() {
            @Override
            public boolean hasPrevious() {
                return base.hasPrevious();
            }

            @Override
            public GHAppInstallation[] previous() {
                return base.previous().getInstallations();
            }

            @Override
            public GHAppInstallation[] first() {
                return base.first().getInstallations();
            }

            @Override
            public GHAppInstallation[] last() {
                return base.last().getInstallations();
            }

            @Override
            public int totalCount() {
                return base.totalCount();
            }

            @Override
            public int currentPage() {
                return base.currentPage();
            }

            public boolean hasNext() {
                return base.hasNext();
            }

            public GHAppInstallation[] next() {
                return base.next().getInstallations();
            }
        };
    }
}
