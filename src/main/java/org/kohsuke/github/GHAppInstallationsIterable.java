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
}
