/*
 * GitHub API for Java
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
