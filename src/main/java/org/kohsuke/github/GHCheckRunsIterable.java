package org.kohsuke.github;

import java.util.Iterator;

import javax.annotation.Nonnull;

// TODO: Auto-generated Javadoc
/**
 * Iterable for check-runs listing.
 */
class GHCheckRunsIterable extends PagedIterable<GHCheckRun> {
    private final GHRepository owner;
    private final GitHubRequest request;

    private GHCheckRunsPage result;

    /**
     * Instantiates a new GH check runs iterable.
     *
     * @param owner the owner
     * @param request the request
     */
    public GHCheckRunsIterable(GHRepository owner, GitHubRequest request) {
        this.owner = owner;
        this.request = request;
    }

    /**
     * Iterator.
     *
     * @param pageSize the page size
     * @return the paged iterator
     */
    @Nonnull
    @Override
    public PagedIterator<GHCheckRun> _iterator(int pageSize) {
        return new PagedIterator<>(
                adapt(GitHubPageIterator.create(owner.root().getClient(), GHCheckRunsPage.class, request, pageSize)),
                null);
    }

    /**
     * Adapt.
     *
     * @param base the base
     * @return the iterator
     */
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
