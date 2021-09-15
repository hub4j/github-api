package org.kohsuke.github;

import java.util.Iterator;

import javax.annotation.Nonnull;

/**
 * Iterable for check-runs listing.
 */
class GHCheckRunsIterable extends PagedIterable<GHCheckRun> {
    private final GHRepository owner;
    private final GitHubRequest request;

    private GHCheckRunsPage result;

    public GHCheckRunsIterable(GHRepository owner, GitHubRequest request) {
        this.owner = owner;
        this.request = request;
    }

    @Nonnull
    @Override
    public PagedIterator<GHCheckRun> _iterator(int pageSize) {
        return new PagedIterator<>(
                adapt(GitHubPageIterator.create(owner.root().getClient(), GHCheckRunsPage.class, request, pageSize)),
                null);
    }

    protected Iterator<GHCheckRun[]> adapt(final Iterator<GHCheckRunsPage> base) {
        return new Iterator<GHCheckRun[]>() {
            public boolean hasNext() {
                return base.hasNext();
            }

            public GHCheckRun[] next() {
                GHCheckRunsPage v = base.next();
                if (result == null) {
                    result = v;
                }
                return v.getCheckRuns(owner);
            }
        };
    }
}
