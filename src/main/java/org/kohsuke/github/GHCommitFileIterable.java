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

    private static PaginatedEndpoint<GHCommitFilesPage, File> createEndpointIterable(GHRepository owner,
            String sha,
            GHCommit.File[] files) {
        PaginatedEndpoint<GHCommitFilesPage, File> iterable;
        if (files != null && files.length < GH_FILE_LIMIT_PER_COMMIT_PAGE) {
            // create a page iterator that only provides one page
            iterable = PaginatedEndpoint.fromSinglePage(new GHCommitFilesPage(files), GHCommit.File.class);
        } else {
            GitHubRequest request = owner.root()
                    .createRequest()
                    .withUrlPath(owner.getApiTailUrl("commits/" + sha))
                    .build();
            iterable = new PaginatedEndpoint<>(owner.root()
                    .getClient(), request, GHCommitFilesPage.class, GHCommit.File.class, null);
        }
        return iterable;
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
    public GHCommitFileIterable(GHRepository owner, String sha, List<GHCommit.File> files) {
        super(createEndpointIterable(owner, sha, files != null ? files.toArray(new File[0]) : null));
    }

    @Override
    public PagedIterable<File> withPageSize(int i) {
        // page size is controlled by the server for this iterable, do not allow it to be set by the caller
        return this;
    }
}
