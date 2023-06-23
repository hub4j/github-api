package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.net.URL;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.kohsuke.github.internal.Previews.ANTIOPE;
import static org.kohsuke.github.internal.Previews.GROOT;

// TODO: Auto-generated Javadoc
/**
 * A commit in a repository.
 *
 * @author Kohsuke Kawaguchi
 * @see GHRepository#getCommit(String) GHRepository#getCommit(String)
 * @see GHCommitComment#getCommit() GHCommitComment#getCommit()
 */
@SuppressFBWarnings(value = { "NP_UNWRITTEN_FIELD", "UWF_UNWRITTEN_FIELD" }, justification = "JSON API")
public class GHCommit {

    /**
     * Number of files returned in the commit response. If there are more files than this, the response will include
     * pagination link headers for the remaining files.
     */
    private static final int GH_FILE_LIMIT_PER_COMMIT_PAGE = 300;

    private GHRepository owner;

    private ShortInfo commit;

    /**
     * Short summary of this commit.
     */
    @SuppressFBWarnings(
            value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD",
                    "UWF_UNWRITTEN_FIELD" },
            justification = "JSON API")
    public static class ShortInfo extends GitCommit {

        private int comment_count = -1;

        /**
         * Gets comment count.
         *
         * @return the comment count
         * @throws GHException
         *             the GH exception
         */
        public int getCommentCount() throws GHException {
            if (comment_count < 0) {
                throw new GHException("Not available on this endpoint.");
            }
            return comment_count;
        }

        /**
         * Creates instance of {@link GHCommit.ShortInfo}.
         */
        public ShortInfo() {
            // Empty constructor required for Jackson binding
        };

        /**
         * Instantiates a new short info.
         *
         * @param commit
         *            the commit
         */
        ShortInfo(GitCommit commit) {
            // Inherited copy constructor, used for bridge method from {@link GitCommit},
            // which is used in {@link GHContentUpdateResponse}) to {@link GHCommit}.
            super(commit);
        }

        /**
         * Gets the parent SHA 1 s.
         *
         * @return the parent SHA 1 s
         */
        @Override
        public List<String> getParentSHA1s() {
            List<String> shortInfoParents = super.getParentSHA1s();
            if (shortInfoParents == null) {
                throw new GHException("Not available on this endpoint. Try calling getParentSHA1s from outer class.");
            }
            return shortInfoParents;
        }

    }

    /**
     * The type GHAuthor.
     *
     * @deprecated Use {@link GitUser} instead.
     */
    @Deprecated
    public static class GHAuthor extends GitUser {

        /**
         * Instantiates a new GH author.
         */
        public GHAuthor() {
            super();
        }

        /**
         * Instantiates a new GH author.
         *
         * @param user
         *            the user
         */
        public GHAuthor(GitUser user) {
            super(user);
        }
    }

    /**
     * The type Stats.
     */
    public static class Stats {

        /** The deletions. */
        int total, additions, deletions;
    }

    /**
     * A file that was modified.
     */
    @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "It's being initialized by JSON deserialization")
    public static class File {

        /** The status. */
        String status;

        /** The deletions. */
        int changes, additions, deletions;

        /** The patch. */
        String raw_url, blob_url, sha, patch;

        /** The previous filename. */
        String filename, previous_filename;

        /**
         * Gets lines changed.
         *
         * @return Number of lines added + removed.
         */
        public int getLinesChanged() {
            return changes;
        }

        /**
         * Gets lines added.
         *
         * @return Number of lines added.
         */
        public int getLinesAdded() {
            return additions;
        }

        /**
         * Gets lines deleted.
         *
         * @return Number of lines removed.
         */
        public int getLinesDeleted() {
            return deletions;
        }

        /**
         * Gets status.
         *
         * @return "modified", "added", or "removed"
         */
        public String getStatus() {
            return status;
        }

        /**
         * Gets file name.
         *
         * @return Full path in the repository.
         */
        @SuppressFBWarnings(value = "NM_CONFUSING",
                justification = "It's a part of the library's API and cannot be renamed")
        public String getFileName() {
            return filename;
        }

        /**
         * Gets previous filename.
         *
         * @return Previous path, in case file has moved.
         */
        public String getPreviousFilename() {
            return previous_filename;
        }

        /**
         * Gets patch.
         *
         * @return The actual change.
         */
        public String getPatch() {
            return patch;
        }

        /**
         * Gets raw url.
         *
         * @return URL like
         *         'https://raw.github.com/jenkinsci/jenkins/4eb17c197dfdcf8ef7ff87eb160f24f6a20b7f0e/core/pom.xml' that
         *         resolves to the actual content of the file.
         */
        public URL getRawUrl() {
            return GitHubClient.parseURL(raw_url);
        }

        /**
         * Gets blob url.
         *
         * @return URL like
         *         'https://github.com/jenkinsci/jenkins/blob/1182e2ebb1734d0653142bd422ad33c21437f7cf/core/pom.xml'
         *         that resolves to the HTML page that describes this file.
         */
        public URL getBlobUrl() {
            return GitHubClient.parseURL(blob_url);
        }

        /**
         * Gets sha.
         *
         * @return [0 -9a-f]{40} SHA1 checksum.
         */
        public String getSha() {
            return sha;
        }
    }

    /**
     * The type Parent.
     */
    public static class Parent {

        /** The url. */
        @SuppressFBWarnings(value = "UUF_UNUSED_FIELD", justification = "We don't provide it in API now")
        String url;

        /** The sha. */
        String sha;
    }

    /**
     * The Class User.
     */
    static class User {

        /** The gravatar id. */
        // TODO: what if someone who doesn't have an account on GitHub makes a commit?
        @SuppressFBWarnings(value = "UUF_UNUSED_FIELD", justification = "We don't provide it in API now")
        String url, avatar_url, gravatar_id;

        /** The id. */
        @SuppressFBWarnings(value = "UUF_UNUSED_FIELD", justification = "We don't provide it in API now")
        int id;

        /** The login. */
        String login;
    }

    /** The sha. */
    String url, html_url, sha, message;

    /** The files. */
    List<File> files;

    /** The stats. */
    Stats stats;

    /** The parents. */
    List<Parent> parents;

    /** The committer. */
    User author, committer;

    /**
     * Creates an instance of {@link GHCommit}.
     */
    public GHCommit() {
        // empty constructor needed for Jackson binding
    }

    /**
     * Instantiates a new GH commit.
     *
     * @param shortInfo
     *            the short info
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "acceptable")
    GHCommit(ShortInfo shortInfo) {
        // Constructs a (relatively sparse) GHCommit from a GitCommit. Used for
        // bridge method from {@link GitCommit}, which is used in
        // {@link GHContentUpdateResponse}) to {@link GHCommit}.
        commit = shortInfo;

        owner = commit.getOwner();
        html_url = commit.getHtmlUrl();
        sha = commit.getSha();
        url = commit.getUrl();
        parents = commit.getParents();
        message = commit.getMessage();
    }

    /**
     * Gets commit short info.
     *
     * @return the commit short info
     * @throws IOException
     *             the io exception
     */
    public ShortInfo getCommitShortInfo() throws IOException {
        if (commit == null)
            populate();
        return commit;
    }

    /**
     * Gets owner.
     *
     * @return the repository that contains the commit.
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHRepository getOwner() {
        return owner;
    }

    /**
     * Gets lines changed.
     *
     * @return the number of lines added + removed.
     * @throws IOException
     *             if the field was not populated and refresh fails
     */
    public int getLinesChanged() throws IOException {
        populate();
        return stats.total;
    }

    /**
     * Gets lines added.
     *
     * @return Number of lines added.
     * @throws IOException
     *             if the field was not populated and refresh fails
     */
    public int getLinesAdded() throws IOException {
        populate();
        return stats.additions;
    }

    /**
     * Gets lines deleted.
     *
     * @return Number of lines removed.
     * @throws IOException
     *             if the field was not populated and refresh fails
     */
    public int getLinesDeleted() throws IOException {
        populate();
        return stats.deletions;
    }

    /**
     * Use this method to walk the tree.
     *
     * @return a GHTree to walk
     * @throws IOException
     *             on error
     */
    public GHTree getTree() throws IOException {
        return owner.getTree(getCommitShortInfo().getTreeSHA1());
    }

    /**
     * Gets html url.
     *
     * @return URL of this commit like
     *         "https://github.com/kohsuke/sandbox-ant/commit/8ae38db0ea5837313ab5f39d43a6f73de3bd9000"
     */
    public URL getHtmlUrl() {
        return GitHubClient.parseURL(html_url);
    }

    /**
     * Gets sha 1.
     *
     * @return [0 -9a-f]{40} SHA1 checksum.
     */
    public String getSHA1() {
        return sha;
    }

    /**
     * Gets url.
     *
     * @return API URL of this object.
     */
    public URL getUrl() {
        return GitHubClient.parseURL(url);
    }

    /**
     * List of files changed/added/removed in this commit.
     *
     * @return Can be empty but never null.
     * @throws IOException
     *             on error
     * @deprecated Use {@link #listFiles()} instead.
     */
    @Deprecated
    public List<File> getFiles() throws IOException {
        return listFiles();
    }

    /**
     * List of files changed/added/removed in this commit. Uses a paginated list if the files returned by GitHub exceed
     * 300 in quantity.
     *
     * @return the List of files
     * @see <a href="https://docs.github.com/en/rest/commits/commits?apiVersion=2022-11-28#get-a-commit">Get a
     *      commit</a>
     * @throws IOException
     *             on error
     */
    public List<File> listFiles() throws IOException {

        populate();

        if (files != null && files.size() < GH_FILE_LIMIT_PER_COMMIT_PAGE) {
            return Collections.unmodifiableList(files);
        }

        PagedIterable<File> filesIterable = new GHCommitIterable(owner, sha);
        if (files == null) {
            files = new ArrayList<>();
        }
        files.clear();
        files.addAll(filesIterable.toList());
        return Collections.unmodifiableList(files);
    }

    /**
     * Gets parent sha 1 s.
     *
     * @return SHA1 of parent commit objects.
     */
    public List<String> getParentSHA1s() {
        if (parents == null || parents.size() == 0)
            return Collections.emptyList();
        return new AbstractList<String>() {
            @Override
            public String get(int index) {
                return parents.get(index).sha;
            }

            @Override
            public int size() {
                return parents.size();
            }
        };
    }

    /**
     * Resolves the parent commit objects and return them.
     *
     * @return parent commit objects
     * @throws IOException
     *             on error
     */
    public List<GHCommit> getParents() throws IOException {
        populate();
        List<GHCommit> r = new ArrayList<GHCommit>();
        for (String sha1 : getParentSHA1s())
            r.add(owner.getCommit(sha1));
        return r;
    }

    /**
     * Gets author.
     *
     * @return the author
     * @throws IOException
     *             the io exception
     */
    public GHUser getAuthor() throws IOException {
        populate();
        return resolveUser(author);
    }

    /**
     * Gets the date the change was authored on.
     *
     * @return the date the change was authored on.
     * @throws IOException
     *             if the information was not already fetched and an attempt at fetching the information failed.
     */
    public Date getAuthoredDate() throws IOException {
        return getCommitShortInfo().getAuthoredDate();
    }

    /**
     * Gets committer.
     *
     * @return the committer
     * @throws IOException
     *             the io exception
     */
    public GHUser getCommitter() throws IOException {
        populate();
        return resolveUser(committer);
    }

    /**
     * Gets the date the change was committed on.
     *
     * @return the date the change was committed on.
     * @throws IOException
     *             if the information was not already fetched and an attempt at fetching the information failed.
     */
    public Date getCommitDate() throws IOException {
        return getCommitShortInfo().getCommitDate();
    }

    private GHUser resolveUser(User author) throws IOException {
        if (author == null || author.login == null)
            return null;
        return owner.root().getUser(author.login);
    }

    /**
     * Retrieves a list of pull requests which contain this commit.
     *
     * @return {@link PagedIterable} with the pull requests which contain this commit
     */
    @Preview(GROOT)
    public PagedIterable<GHPullRequest> listPullRequests() {
        return owner.root()
                .createRequest()
                .withPreview(GROOT)
                .withUrlPath(String.format("/repos/%s/%s/commits/%s/pulls", owner.getOwnerName(), owner.getName(), sha))
                .toIterable(GHPullRequest[].class, item -> item.wrapUp(owner));
    }

    /**
     * Retrieves a list of branches where this commit is the head commit.
     *
     * @return {@link PagedIterable} with the branches where the commit is the head commit
     * @throws IOException
     *             the io exception
     */
    @Preview(GROOT)
    public PagedIterable<GHBranch> listBranchesWhereHead() throws IOException {
        return owner.root()
                .createRequest()
                .withPreview(GROOT)
                .withUrlPath(String.format("/repos/%s/%s/commits/%s/branches-where-head",
                        owner.getOwnerName(),
                        owner.getName(),
                        sha))
                .toIterable(GHBranch[].class, item -> item.wrap(owner));
    }

    /**
     * List comments paged iterable.
     *
     * @return {@link PagedIterable} with all the commit comments in this repository.
     */
    public PagedIterable<GHCommitComment> listComments() {
        return owner.listCommitComments(sha);
    }

    /**
     * Creates a commit comment.
     * <p>
     * I'm not sure how path/line/position parameters interact with each other.
     *
     * @param body
     *            body of the comment
     * @param path
     *            path of file being commented on
     * @param line
     *            target line for comment
     * @param position
     *            position on line
     * @return created GHCommitComment
     * @throws IOException
     *             if comment is not created
     */
    public GHCommitComment createComment(String body, String path, Integer line, Integer position) throws IOException {
        GHCommitComment r = owner.root()
                .createRequest()
                .method("POST")
                .with("body", body)
                .with("path", path)
                .with("line", line)
                .with("position", position)
                .withUrlPath(
                        String.format("/repos/%s/%s/commits/%s/comments", owner.getOwnerName(), owner.getName(), sha))
                .fetch(GHCommitComment.class);
        return r.wrap(owner);
    }

    /**
     * Create comment gh commit comment.
     *
     * @param body
     *            the body
     * @return the gh commit comment
     * @throws IOException
     *             the io exception
     */
    public GHCommitComment createComment(String body) throws IOException {
        return createComment(body, null, null, null);
    }

    /**
     * List statuses paged iterable.
     *
     * @return status of this commit, newer ones first.
     * @throws IOException
     *             if statuses cannot be read
     */
    public PagedIterable<GHCommitStatus> listStatuses() throws IOException {
        return owner.listCommitStatuses(sha);
    }

    /**
     * Gets last status.
     *
     * @return the last status of this commit, which is what gets shown in the UI.
     * @throws IOException
     *             on error
     */
    public GHCommitStatus getLastStatus() throws IOException {
        return owner.getLastCommitStatus(sha);
    }

    /**
     * Gets check-runs for given sha.
     *
     * @return check runs for given sha.
     * @throws IOException
     *             on error
     */
    @Preview(ANTIOPE)
    public PagedIterable<GHCheckRun> getCheckRuns() throws IOException {
        return owner.getCheckRuns(sha);
    }

    /**
     * Some of the fields are not always filled in when this object is retrieved as a part of another API call.
     *
     * @throws IOException
     *             on error
     */
    void populate() throws IOException {
        if (files == null && stats == null)
            owner.root().createRequest().withUrlPath(owner.getApiTailUrl("commits/" + sha)).fetchInto(this);
    }

    /**
     * Wrap up.
     *
     * @param owner
     *            the owner
     * @return the GH commit
     */
    GHCommit wrapUp(GHRepository owner) {
        this.owner = owner;
        return this;
    }

}
