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
 * @see GHPullRequest#listCommits()
 */
@SuppressFBWarnings(value = {"UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD",
    "NP_UNWRITTEN_FIELD", "URF_UNREAD_FIELD"}, justification = "JSON API")
public class GHPullRequestCommitDetail {
    private GHPullRequest owner;

    /*package*/ void wrapUp(GHPullRequest owner) {
        this.owner = owner;
    }

    /**
     * @deprecated Use {@link GitUser}
     */
    public static class Authorship extends GitUser {
    }

    public static class Tree {
        String sha;
        String url;

        public String getSha() {
            return sha;
        }

        public URL getUrl() {
            return GitHub.parseURL(url);
        }
    }

    public static class Commit {
        Authorship author;
        Authorship committer;
        String message;
        Tree tree;
        String url;
        int comment_count;

        @WithBridgeMethods(value = Authorship.class, castRequired = true)
        public GitUser getAuthor() {
            return author;
        }

        @WithBridgeMethods(value = Authorship.class, castRequired = true)
        public GitUser getCommitter() {
            return committer;
        }

        public String getMessage() {
            return message;
        }

        public URL getUrl() {
            return GitHub.parseURL(url);
        }

        public int getComment_count() {
            return comment_count;
        }

        public Tree getTree() {
            return tree;
        }
    }

    public static class CommitPointer {
        String sha;
        String url;
        String html_url;

        public URL getUrl() {
            return GitHub.parseURL(url);
        }

        public URL getHtml_url() {
            return GitHub.parseURL(html_url);
        }

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

    public String getSha() {
        return sha;
    }

    public Commit getCommit() {
        return commit;
    }

    public URL getApiUrl() {
        return GitHub.parseURL(url);
    }

    public URL getUrl() {
        return GitHub.parseURL(html_url);
    }

    public URL getCommentsUrl() {
        return GitHub.parseURL(comments_url);
    }

    public CommitPointer[] getParents() {
        CommitPointer[] newValue = new CommitPointer[parents.length];
        System.arraycopy(parents, 0, newValue, 0, parents.length);
        return newValue;
    }
}
