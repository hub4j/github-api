package org.kohsuke.github;

import java.util.Iterator;

import javax.annotation.Nonnull;

class GHCodeScanningAlertsIterable extends PagedIterable<GHCodeScanningAlert> {
    private final GHRepository owner;
    private final GitHubRequest request;
    private GHCodeScanningAlert[] result;

    GHCodeScanningAlertsIterable(GHRepository owner, GitHubRequest request) {
        this.owner = owner;
        this.request = request;
    }

    @Nonnull
    @Override
    public PagedIterator<GHCodeScanningAlert> _iterator(int pageSize) {
        return new PagedIterator<>(
                adapt(GitHubPageIterator
                        .create(owner.root().getClient(), GHCodeScanningAlert[].class, request, pageSize)),
                null);
    }

    protected Iterator<GHCodeScanningAlert[]> adapt(final Iterator<GHCodeScanningAlert[]> base) {
        return new Iterator<GHCodeScanningAlert[]>() {
            public boolean hasNext() {
                return base.hasNext();
            }

            public GHCodeScanningAlert[] next() {
                GHCodeScanningAlert[] v = base.next();
                if (result == null) {
                    result = v;
                }

                for (GHCodeScanningAlert alert : result) {
                    alert.wrap(owner);
                }
                return result;
            }
        };
    }
}
