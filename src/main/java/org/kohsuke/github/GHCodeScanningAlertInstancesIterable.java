package org.kohsuke.github;

import java.net.MalformedURLException;
import java.util.Iterator;

import javax.annotation.Nonnull;

public class GHCodeScanningAlertInstancesIterable extends PagedIterable<GHCodeScanningAlertInstance> {
    private final GHCodeScanningAlert owner;
    private final GitHubRequest request;
    private GHCodeScanningAlertInstance[] result;

    public GHCodeScanningAlertInstancesIterable(GHCodeScanningAlert owner, GitHubRequest.Builder<?> requestBuilder) {
        this.owner = owner;
        try {
            this.request = requestBuilder.build();
        } catch (MalformedURLException e) {
            throw new GHException("Malformed URL", e);
        }
    }

    @Nonnull
    @Override
    public PagedIterator<GHCodeScanningAlertInstance> _iterator(int pageSize) {
        return new PagedIterator<>(
                adapt(GitHubPageIterator
                        .create(owner.getRoot().getClient(), GHCodeScanningAlertInstance[].class, request, pageSize)),
                null);
    }

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
