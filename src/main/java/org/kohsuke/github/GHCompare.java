package org.kohsuke.github;

import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.net.URL;

/**
 * The model user for comparing 2 commits in the GitHub API.
 *
 * @author Michael Clarke
 */
public class GHCompare {

    private String url, html_url, permalink_url, diff_url, patch_url;
    public Status status;
    private int ahead_by, behind_by, total_commits;
    private Commit base_commit, merge_base_commit;
    private Commit[] commits;
    private GHCommit.File[] files;

    private GHRepository owner;

    /**
     * Gets url.
     *
     * @return the url
     */
    public URL getUrl() {
        return GitHub.parseURL(url);
    }

    /**
     * Gets html url.
     *
     * @return the html url
     */
    public URL getHtmlUrl() {
        return GitHub.parseURL(html_url);
    }

    /**
     * Gets permalink url.
     *
     * @return the permalink url
     */
    public URL getPermalinkUrl() {
        return GitHub.parseURL(permalink_url);
    }

    /**
     * Gets diff url.
     *
     * @return the diff url
     */
    public URL getDiffUrl() {
        return GitHub.parseURL(diff_url);
    }

    /**
     * Gets patch url.
     *
     * @return the patch url
     */
    public URL getPatchUrl() {
        return GitHub.parseURL(patch_url);
    }

    /**
     * Gets status.
     *
     * @return the status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Gets ahead by.
     *
     * @return the ahead by
     */
    public int getAheadBy() {
        return ahead_by;
    }

    /**
     * Gets behind by.
     *
     * @return the behind by
     */
    public int getBehindBy() {
        return behind_by;
    }

    /**
     * Gets total commits.
     *
     * @return the total commits
     */
    public int getTotalCommits() {
        return total_commits;
    }

    /**
     * Gets base commit.
     *
     * @return the base commit
     */
    public Commit getBaseCommit() {
        return base_commit;
    }

    /**
     * Gets merge base commit.
     *
     * @return the merge base commit
     */
    public Commit getMergeBaseCommit() {
        return merge_base_commit;
    }

    /**
     * Gets an array of commits.
     *
     * @return A copy of the array being stored in the class.
     */
    public Commit[] getCommits() {
        Commit[] newValue = new Commit[commits.length];
        System.arraycopy(commits, 0, newValue, 0, commits.length);
        return newValue;
    }

    /**
     * Gets an array of commits.
     *
     * @return A copy of the array being stored in the class.
     */
    public GHCommit.File[] getFiles() {
        GHCommit.File[] newValue = new GHCommit.File[files.length];
        System.arraycopy(files, 0, newValue, 0, files.length);
        return newValue;
    }

    /**
     * Wrap gh compare.
     *
     * @param owner
     *            the owner
     * @return the gh compare
     */
    public GHCompare wrap(GHRepository owner) {
        this.owner = owner;
        for (Commit commit : commits) {
            commit.wrapUp(owner);
        }
        merge_base_commit.wrapUp(owner);
        base_commit.wrapUp(owner);
        return this;
    }

    /**
     * Compare commits had a child commit element with additional details we want to capture. This extenstion of
     * GHCommit provides that.
     */
    @SuppressFBWarnings(value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD" },
            justification = "JSON API")
    public static class Commit extends GHCommit {

        private InnerCommit commit;

        /**
         * Gets commit.
         *
         * @return the commit
         */
        public InnerCommit getCommit() {
            return commit;
        }
    }

    /**
     * The type InnerCommit.
     */
    public static class InnerCommit {
        private String url, sha, message;
        private User author, committer;
        private Tree tree;

        /**
         * Gets url.
         *
         * @return the url
         */
        public String getUrl() {
            return url;
        }

        /**
         * Gets sha.
         *
         * @return the sha
         */
        public String getSha() {
            return sha;
        }

        /**
         * Gets message.
         *
         * @return the message
         */
        public String getMessage() {
            return message;
        }

        /**
         * Gets author.
         *
         * @return the author
         */
        @WithBridgeMethods(value = User.class, castRequired = true)
        public GitUser getAuthor() {
            return author;
        }

        /**
         * Gets committer.
         *
         * @return the committer
         */
        @WithBridgeMethods(value = User.class, castRequired = true)
        public GitUser getCommitter() {
            return committer;
        }

        /**
         * Gets tree.
         *
         * @return the tree
         */
        public Tree getTree() {
            return tree;
        }
    }

    /**
     * The type Tree.
     */
    public static class Tree {
        private String url, sha;

        /**
         * Gets url.
         *
         * @return the url
         */
        public String getUrl() {
            return url;
        }

        /**
         * Gets sha.
         *
         * @return the sha
         */
        public String getSha() {
            return sha;
        }
    }

    /**
     * The type User.
     *
     * @deprecated use {@link GitUser} instead.
     */
    public static class User extends GitUser {
    }

    /**
     * The enum Status.
     */
    public static enum Status {
        behind, ahead, identical, diverged
    }
}
