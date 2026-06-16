package org.kohsuke.github;

import org.kohsuke.github.GHCommit.File;

import java.util.List;

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

    private static PaginatedEndpoint<GHCommitFilesPage, File> createEndpoint(GHRepository owner,
            String sha,
            List<GHCommit.File> files) {
        PaginatedEndpoint<GHCommitFilesPage, File> endpoint;
        if (files != null && files.size() < GH_FILE_LIMIT_PER_COMMIT_PAGE) {
            // create an endpoint that only reads one already loaded page
            endpoint = PaginatedEndpoint.fromSinglePage(new GHCommitFilesPage(files.toArray(new File[0])),
                    GHCommit.File.class);
        } else {
            endpoint = owner.root()
                    .createRequest()
                    .withUrlPath(owner.getApiTailUrl("commits/" + sha))
                    .toPaginatedEndpoint(GHCommitFilesPage.class, GHCommit.File.class, null);
        }
        return endpoint;
    }

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
    GHCommitFileIterable(GHRepository owner, String sha, List<GHCommit.File> files) {
        super(createEndpoint(owner, sha, files));
    }

    @Override
    public PagedIterable<File> withPageSize(int i) {
        // page size is controlled by the server for this iterable, do not allow it to be set by the caller
        return this;
    }
}
