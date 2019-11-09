package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JacksonInject;
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

    @JacksonInject(value = "org.kohsuke.github.GHRepository")
    private GHRepository owner;

    public URL getUrl() {
        return GitHub.parseURL(url);
    }

    public URL getHtmlUrl() {
        return GitHub.parseURL(html_url);
    }

    public URL getPermalinkUrl() {
        return GitHub.parseURL(permalink_url);
    }

    public URL getDiffUrl() {
        return GitHub.parseURL(diff_url);
    }

    public URL getPatchUrl() {
        return GitHub.parseURL(patch_url);
    }

    public Status getStatus() {
        return status;
    }

    public int getAheadBy() {
        return ahead_by;
    }

    public int getBehindBy() {
        return behind_by;
    }

    public int getTotalCommits() {
        return total_commits;
    }

    public Commit getBaseCommit() {
        return base_commit;
    }

    public Commit getMergeBaseCommit() {
        return merge_base_commit;
    }

    /**
     * Gets an array of commits.
     * @return A copy of the array being stored in the class.
     */
    public Commit[] getCommits() {
        Commit[] newValue = new Commit[commits.length];
        System.arraycopy(commits, 0, newValue, 0, commits.length);
        return newValue;
    }
    
    /**
     * Gets an array of commits.
     * @return A copy of the array being stored in the class.
     */
    public GHCommit.File[] getFiles() {
        GHCommit.File[] newValue = new GHCommit.File[files.length];
        System.arraycopy(files, 0, newValue, 0, files.length);
        return newValue;
    }

    /**
     * Compare commits had a child commit element with additional details we want to capture.
     * This extenstion of GHCommit provides that.
     */
    @SuppressFBWarnings(value = {"UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD"}, 
            justification = "JSON API")
    public static class Commit extends GHCommit {

        private InnerCommit commit;

        public InnerCommit getCommit() {
            return commit;
        }
    }


    public static class InnerCommit {
        private String url, sha, message;
        private User author, committer;
        private Tree tree;

        public String getUrl() {
            return url;
        }

        public String getSha() {
            return sha;
        }

        public String getMessage() {
            return message;
        }

        @WithBridgeMethods(value=User.class,castRequired=true)
        public GitUser getAuthor() {
            return author;
        }

        @WithBridgeMethods(value=User.class,castRequired=true)
        public GitUser getCommitter() {
            return committer;
        }

        public Tree getTree() {
            return tree;
        }
    }

    public static class Tree {
        private String url, sha;

        public String getUrl() {
            return url;
        }

        public String getSha() {
            return sha;
        }
    }

    /**
     * @deprecated use {@link GitUser} instead.
     */
    public static class User extends GitUser {
    }

    public static enum Status {
        behind, ahead, identical, diverged
    }
}
