package org.kohsuke.github;

import java.util.Iterator;

import javax.annotation.Nonnull;

/**
 * Iterable for commit listing.
 *
 * @author Stephen Horgan
 */
class GHCommitIterable extends PagedIterable<GHCommit.File> {

    private final GHRepository owner;
    private final String sha;
    private GHCommitFilesPage result;

    /**
     * Instantiates a new GH commit iterable.
     *
     * @param owner
     *            the owner
     * @param sha
     *            the SHA of the commit
     */
    public GHCommitIterable(GHRepository owner, String sha) {
        this.owner = owner;
        this.sha = sha;
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
    public PagedIterator<GHCommit.File> _iterator(int pageSize) {

        GitHubRequest request = owner.root().createRequest().withUrlPath(owner.getApiTailUrl("commits/" + sha)).build();

        return new PagedIterator<>(
                adapt(GitHubPageIterator.create(owner.root().getClient(), GHCommitFilesPage.class, request, pageSize)),
                null);
    }

    /**
     * Adapt.
     *
     * @param base
     *            the base commit page
     * @return the iterator
     */
    protected Iterator<GHCommit.File[]> adapt(final Iterator<GHCommitFilesPage> base) {
        return new Iterator<GHCommit.File[]>() {

            public boolean hasNext() {
                return base.hasNext();
            }

            public GHCommit.File[] next() {
                GHCommitFilesPage v = base.next();
                if (result == null) {
                    result = v;
                }
                return v.getFiles();
            }
        };
    }
}
