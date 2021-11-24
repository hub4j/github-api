package org.kohsuke.github;

import java.util.Iterator;

import javax.annotation.Nonnull;

/**
 * Iterable for GHAppInstallation listing.
 */
class GHAppInstallationsIterable extends PagedIterable<GHAppInstallation> {
    public static final String APP_INSTALLATIONS_URL = "/user/installations";
    private final transient GitHub root;
    private GHAppInstallationsPage result;

    public GHAppInstallationsIterable(GitHub root) {
        this.root = root;
    }

    @Nonnull
    @Override
    public PagedIterator<GHAppInstallation> _iterator(int pageSize) {
        final GitHubRequest request = root.createRequest().withUrlPath(APP_INSTALLATIONS_URL).build();
        return new PagedIterator<>(
                adapt(GitHubPageIterator.create(root.getClient(), GHAppInstallationsPage.class, request, pageSize)),
                null);
    }

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
