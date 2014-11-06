package org.kohsuke.github;

import com.infradna.tool.bridge_method_injector.WithBridgeMethods;

import java.net.URL;
import java.util.Date;

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

    public Commit[] getCommits() {
        return commits;
    }
    
    public GHCommit.File[] getFiles() {
		return files;
	}

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
     * Compare commits had a child commit element with additional details we want to capture.
     * This extenstion of GHCommit provides that.
     */
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
