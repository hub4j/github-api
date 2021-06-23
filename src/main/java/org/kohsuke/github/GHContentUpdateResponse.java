package org.kohsuke.github;

import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.net.URL;

/**
 * The response that is returned when updating repository content.
 */
public class GHContentUpdateResponse {
    private GHContent content;
    private Commit commit;

    /**
     * Gets content.
     *
     * @return the content
     */
    public GHContent getContent() {
        return content;
    }

    /**
     * Gets commit.
     *
     * @return the commit
     */
    @WithBridgeMethods(value = GHCommit.class, adapterMethod = "returnNullForGHCommit")
    public Commit getCommit() {
        return commit;
    }

    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "Bridge method of getCommit")
    private Object returnNullForGHCommit(Commit commit, Class type) {
        return null;
    }

    /**
     * The type Commit.
     */
    public static class Commit {
        String sha;
        GitUser author;
        GitUser committer;
        String message;
        Tree tree;
        String url;

        /**
         * Gets sha.
         *
         * @return the sha
         */
        public String getSha() {
            return this.sha;
        }

        /**
         * Gets author.
         *
         * @return the author
         */
        @WithBridgeMethods(value = GHPullRequestCommitDetail.Authorship.class, castRequired = true)
        public GitUser getAuthor() {
            return author;
        }

        /**
         * Gets committer.
         *
         * @return the committer
         */
        @WithBridgeMethods(value = GHPullRequestCommitDetail.Authorship.class, castRequired = true)
        public GitUser getCommitter() {
            return committer;
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
         * Gets url.
         *
         * @return the url
         */
        public URL getUrl() {
            return GitHubClient.parseURL(url);
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
        String sha;
        String url;

        /**
         * Gets sha.
         *
         * @return the sha
         */
        public String getSha() {
            return sha;
        }

        /**
         * Gets url.
         *
         * @return the url
         */
        public URL getUrl() {
            return GitHubClient.parseURL(url);
        }
    }
}
