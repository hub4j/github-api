package org.kohsuke.github;

import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.net.URL;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * A commit in a repository.
 *
 * @author Kohsuke Kawaguchi
 * @see GHRepository#getCommit(String)
 * @see GHCommitComment#getCommit()
 */
@SuppressFBWarnings(value = { "NP_UNWRITTEN_FIELD", "UWF_UNWRITTEN_FIELD" }, justification = "JSON API")
public class GHCommit {
    private GHRepository owner;

    private ShortInfo commit;

    /**
     * Short summary of this commit.
     */
    @SuppressFBWarnings(value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD",
            "NP_UNWRITTEN_FIELD", "UWF_UNWRITTEN_FIELD" }, justification = "JSON API")
    public static class ShortInfo {
        private GHAuthor author;
        private GHAuthor committer;

        private String message;

        private int comment_count;

        static class Tree {
            String sha;
        }

        private Tree tree;

        @WithBridgeMethods(value = GHAuthor.class, castRequired = true)
        public GitUser getAuthor() {
            return author;
        }

        public Date getAuthoredDate() {
            return GitHub.parseDate(author.date);
        }

        @WithBridgeMethods(value = GHAuthor.class, castRequired = true)
        public GitUser getCommitter() {
            return committer;
        }

        public Date getCommitDate() {
            return GitHub.parseDate(committer.date);
        }

        /**
         * @return Commit message.
         */
        public String getMessage() {
            return message;
        }

        public int getCommentCount() {
            return comment_count;
        }
    }

    /**
     * @deprecated Use {@link GitUser} instead.
     */
    public static class GHAuthor extends GitUser {
        private String date;
    }

    public static class Stats {
        int total, additions, deletions;
    }

    /**
     * A file that was modified.
     */
    @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "It's being initilized by JSON deserialization")
    public static class File {
        String status;
        int changes, additions, deletions;
        String raw_url, blob_url, sha, patch;
        String filename, previous_filename;

        /**
         * @return Number of lines added + removed.
         */
        public int getLinesChanged() {
            return changes;
        }

        /**
         * @return Number of lines added.
         */
        public int getLinesAdded() {
            return additions;
        }

        /**
         * @return Number of lines removed.
         */
        public int getLinesDeleted() {
            return deletions;
        }

        /**
         * @return "modified", "added", or "removed"
         */
        public String getStatus() {
            return status;
        }

        /**
         * @return Full path in the repository.
         */
        @SuppressFBWarnings(value = "NM_CONFUSING", justification = "It's a part of the library's API and cannot be renamed")
        public String getFileName() {
            return filename;
        }

        /**
         * @return Previous path, in case file has moved.
         */
        public String getPreviousFilename() {
            return previous_filename;
        }

        /**
         * @return The actual change.
         */
        public String getPatch() {
            return patch;
        }

        /**
         * @return URL like
         *         'https://raw.github.com/jenkinsci/jenkins/4eb17c197dfdcf8ef7ff87eb160f24f6a20b7f0e/core/pom.xml' that
         *         resolves to the actual content of the file.
         */
        public URL getRawUrl() {
            return GitHub.parseURL(raw_url);
        }

        /**
         * @return URL like
         *         'https://github.com/jenkinsci/jenkins/blob/1182e2ebb1734d0653142bd422ad33c21437f7cf/core/pom.xml'
         *         that resolves to the HTML page that describes this file.
         */
        public URL getBlobUrl() {
            return GitHub.parseURL(blob_url);
        }

        /**
         * @return [0-9a-f]{40} SHA1 checksum.
         */
        public String getSha() {
            return sha;
        }
    }

    public static class Parent {
        @SuppressFBWarnings(value = "UUF_UNUSED_FIELD", justification = "We don't provide it in API now")
        String url;
        String sha;
    }

    static class User {
        // TODO: what if someone who doesn't have an account on GitHub makes a commit?
        @SuppressFBWarnings(value = "UUF_UNUSED_FIELD", justification = "We don't provide it in API now")
        String url, avatar_url, gravatar_id;
        @SuppressFBWarnings(value = "UUF_UNUSED_FIELD", justification = "We don't provide it in API now")
        int id;

        String login;
    }

    String url, html_url, sha;
    List<File> files;
    Stats stats;
    List<Parent> parents;
    User author, committer;

    public ShortInfo getCommitShortInfo() throws IOException {
        if (commit == null)
            populate();
        return commit;
    }

    /**
     * @return the repository that contains the commit.
     */
    public GHRepository getOwner() {
        return owner;
    }

    /**
     * @return the number of lines added + removed.
     * @throws IOException
     *             if the field was not populated and refresh fails
     */
    public int getLinesChanged() throws IOException {
        populate();
        return stats.total;
    }

    /**
     * @return Number of lines added.
     * @throws IOException
     *             if the field was not populated and refresh fails
     */
    public int getLinesAdded() throws IOException {
        populate();
        return stats.additions;
    }

    /**
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
        return owner.getTree(getCommitShortInfo().tree.sha);
    }

    /**
     * @return URL of this commit like
     *         "https://github.com/kohsuke/sandbox-ant/commit/8ae38db0ea5837313ab5f39d43a6f73de3bd9000"
     */
    public URL getHtmlUrl() {
        return GitHub.parseURL(html_url);
    }

    /**
     * @return [0-9a-f]{40} SHA1 checksum.
     */
    public String getSHA1() {
        return sha;
    }

    /**
     * List of files changed/added/removed in this commit.
     *
     * @return Can be empty but never null.
     * @throws IOException
     *             on error
     */
    public List<File> getFiles() throws IOException {
        populate();
        return files != null ? Collections.unmodifiableList(files) : Collections.<File> emptyList();
    }

    /**
     * @return SHA1 of parent commit objects.
     */
    public List<String> getParentSHA1s() {
        if (parents == null)
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
        List<GHCommit> r = new ArrayList<GHCommit>();
        for (String sha1 : getParentSHA1s())
            r.add(owner.getCommit(sha1));
        return r;
    }

    public GHUser getAuthor() throws IOException {
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

    public GHUser getCommitter() throws IOException {
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
        return owner.root.getUser(author.login);
    }

    /**
     * @return {@link PagedIterable} with all the commit comments in this repository.
     */
    public PagedIterable<GHCommitComment> listComments() {
        return owner.root.retrieve().asPagedIterable(
                String.format("/repos/%s/%s/commits/%s/comments", owner.getOwnerName(), owner.getName(), sha),
                GHCommitComment[].class, item -> item.wrap(owner));
    }

    /**
     * Creates a commit comment.
     *
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
     *
     * @return created GHCommitComment
     * @throws IOException
     *             if comment is not created
     */
    public GHCommitComment createComment(String body, String path, Integer line, Integer position) throws IOException {
        GHCommitComment r = new Requester(owner.root).with("body", body).with("path", path).with("line", line)
                .with("position", position)
                .to(String.format("/repos/%s/%s/commits/%s/comments", owner.getOwnerName(), owner.getName(), sha),
                        GHCommitComment.class);
        return r.wrap(owner);
    }

    public GHCommitComment createComment(String body) throws IOException {
        return createComment(body, null, null, null);
    }

    /**
     * @return status of this commit, newer ones first.
     * @throws IOException
     *             if statuses cannot be read
     */
    public PagedIterable<GHCommitStatus> listStatuses() throws IOException {
        return owner.listCommitStatuses(sha);
    }

    /**
     * @return the last status of this commit, which is what gets shown in the UI.
     * @throws IOException
     *             on error
     */
    public GHCommitStatus getLastStatus() throws IOException {
        return owner.getLastCommitStatus(sha);
    }

    /**
     * Some of the fields are not always filled in when this object is retrieved as a part of another API call.
     * 
     * @throws IOException
     *             on error
     */
    void populate() throws IOException {
        if (files == null && stats == null)
            owner.root.retrieve().to(owner.getApiTailUrl("commits/" + sha), this);
    }

    GHCommit wrapUp(GHRepository owner) {
        this.owner = owner;
        return this;
    }
}
