package org.kohsuke.github;

import java.util.Iterator;

import javax.annotation.Nonnull;

/**
 * Iterable for github code scanning instances.
 */
public class GHCodeScanningAlertInstancesIterable extends PagedIterable<GHCodeScanningAlertInstance> {
    private final GHCodeScanningAlert owner;
    private final GitHubRequest request;
    private GHCodeScanningAlertInstance[] result;

    GHCodeScanningAlertInstancesIterable(GHCodeScanningAlert owner, GitHubRequest request) {
        this.owner = owner;
        this.request = request;
    }

    @Nonnull
    @Override
    public PagedIterator<GHCodeScanningAlertInstance> _iterator(int pageSize) {
        return new PagedIterator<>(
                adapt(GitHubPageIterator
                        .create(owner.root().getClient(), GHCodeScanningAlertInstance[].class, request, pageSize)),
                null);
    }

    /**
     * Adapts {@link Iterator}.
     *
     * @param base
     *            the base
     * @return the iterator
     */
    protected Iterator<GHCodeScanningAlertInstance[]> adapt(final Iterator<GHCodeScanningAlertInstance[]> base) {
        return new Iterator<GHCodeScanningAlertInstance[]>() {
            public boolean hasNext() {
                return base.hasNext();
            }

            public GHCodeScanningAlertInstance[] next() {
                GHCodeScanningAlertInstance[] v = base.next();
                if (result == null) {
                    result = v;
                }
                return result;
            }
        };
    }
}
