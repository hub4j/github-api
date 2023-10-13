package org.kohsuke.github;

import org.kohsuke.github.GHCommit.File;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Iterable for commit listing.
 *
 * @author Stephen Horgan
 */
class GHCommitFileIterable extends PagedIterable<GHCommit.File> {

    /**
     * Number of files returned in the commit response. If there are more files than this, the response will include
     * pagination link headers for the remaining files.
     */
    private static final int GH_FILE_LIMIT_PER_COMMIT_PAGE = 300;

    private final GHRepository owner;
    private final String sha;
    private final File[] files;

    /**
     * Instantiates a new GH commit iterable.
     *
     * @param owner
     *            the owner
     * @param sha
     *            the SHA of the commit
     * @param files
     *            the list of files initially populated
     */
    public GHCommitFileIterable(GHRepository owner, String sha, List<File> files) {
        this.owner = owner;
        this.sha = sha;
        this.files = files != null ? files.toArray(new File[0]) : null;
    }

    @Nonnull
    @Override
    public Paginator<File> _paginator(int pageSize, int startPage) {
        throw new UnsupportedOperationException();
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

        Iterator<GHCommit.File[]> pageIterator;

        if (files != null && files.length < GH_FILE_LIMIT_PER_COMMIT_PAGE) {
            // create a page iterator that only provides one page
            pageIterator = Collections.singleton(files).iterator();
        } else {
            // page size is controlled by the server for this iterator, do not allow it to be set by the caller
            pageSize = 0;

            GitHubRequest request = owner.root()
                    .createRequest()
                    .withUrlPath(owner.getApiTailUrl("commits/" + sha))
                    .build();

            pageIterator = adapt(
                    GitHubPageIterator.create(owner.root().getClient(), GHCommitFilesPage.class, request, pageSize));
        }

        return new PagedIterator<>(pageIterator, null);
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
                return v.getFiles();
            }
        };
    }
}
