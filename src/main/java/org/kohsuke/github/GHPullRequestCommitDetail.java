/*
 * The MIT License
 *
 * Copyright (c) 2013, Luca Milanesio
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.kohsuke.github;

import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.net.URL;

/**
 * Commit detail inside a {@link GHPullRequest}.
 *
 * @author Luca Milanesio
 * @see GHPullRequest#listCommits() GHPullRequest#listCommits()
 */
@SuppressFBWarnings(
        value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD",
                "URF_UNREAD_FIELD" },
        justification = "JSON API")
public class GHPullRequestCommitDetail {
    private GHPullRequest owner;

    void wrapUp(GHPullRequest owner) {
        this.owner = owner;
    }

    /**
     * The type Authorship.
     *
     * @deprecated Use {@link GitUser}
     */
    public static class Authorship extends GitUser {
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

    /**
     * The type Commit.
     */
    public static class Commit {
        Authorship author;
        Authorship committer;
        String message;
        Tree tree;
        String url;
        int comment_count;

        /**
         * Gets author.
         *
         * @return the author
         */
        @WithBridgeMethods(value = Authorship.class, castRequired = true)
        public GitUser getAuthor() {
            return author;
        }

        /**
         * Gets committer.
         *
         * @return the committer
         */
        @WithBridgeMethods(value = Authorship.class, castRequired = true)
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
         * Gets comment count.
         *
         * @return the comment count
         */
        public int getComment_count() {
            return comment_count;
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
     * The type CommitPointer.
     */
    public static class CommitPointer {
        String sha;
        String url;
        String html_url;

        /**
         * Gets url.
         *
         * @return the url
         */
        public URL getUrl() {
            return GitHubClient.parseURL(url);
        }

        /**
         * Gets html url.
         *
         * @return the html url
         */
        public URL getHtml_url() {
            return GitHubClient.parseURL(html_url);
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

    String sha;
    Commit commit;
    String url;
    String html_url;
    String comments_url;
    CommitPointer[] parents;

    /**
     * Gets sha.
     *
     * @return the sha
     */
    public String getSha() {
        return sha;
    }

    /**
     * Gets commit.
     *
     * @return the commit
     */
    public Commit getCommit() {
        return commit;
    }

    /**
     * Gets api url.
     *
     * @return the api url
     */
    public URL getApiUrl() {
        return GitHubClient.parseURL(url);
    }

    /**
     * Gets url.
     *
     * @return the url
     */
    public URL getUrl() {
        return GitHubClient.parseURL(html_url);
    }

    /**
     * Gets comments url.
     *
     * @return the comments url
     */
    public URL getCommentsUrl() {
        return GitHubClient.parseURL(comments_url);
    }

    /**
     * Get parents commit pointer [ ].
     *
     * @return the commit pointer [ ]
     */
    public CommitPointer[] getParents() {
        CommitPointer[] newValue = new CommitPointer[parents.length];
        System.arraycopy(parents, 0, newValue, 0, parents.length);
        return newValue;
    }
}
