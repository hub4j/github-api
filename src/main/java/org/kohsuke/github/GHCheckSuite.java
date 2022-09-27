package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * Represents a check suite.
 *
 * @see <a href="https://developer.github.com/v3/checks/suites/">documentation</a>
 */
@SuppressFBWarnings(value = { "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD", "URF_UNREAD_FIELD" },
        justification = "JSON API")
public class GHCheckSuite extends GHObject {

    /** The owner. */
    @JsonProperty("repository")
    GHRepository owner;

    private String nodeId;
    private String headBranch;
    private String headSha;
    private String status;
    private String conclusion;
    private String before;
    private String after;
    private int latestCheckRunsCount;
    private String checkRunsUrl;
    private HeadCommit headCommit;
    private GHApp app;
    private GHPullRequest[] pullRequests;

    /**
     * Wrap.
     *
     * @param owner
     *            the owner
     * @return the GH check suite
     */
    GHCheckSuite wrap(GHRepository owner) {
        this.owner = owner;
        this.wrap(owner.root());
        return this;
    }

    /**
     * Wrap.
     *
     * @param root
     *            the root
     * @return the GH check suite
     */
    GHCheckSuite wrap(GitHub root) {
        if (owner != null) {
            if (pullRequests != null && pullRequests.length != 0) {
                for (GHPullRequest singlePull : pullRequests) {
                    singlePull.wrap(owner);
                }
            }
        }
        return this;
    }

    /**
     * Wrap.
     *
     * @return the GH pull request[]
     */
    GHPullRequest[] wrap() {
        return pullRequests;
    }

    /**
     * Gets the global node id to access most objects in GitHub.
     *
     * @return global node id
     * @see <a href="https://developer.github.com/v4/guides/using-global-node-ids/">documentation</a>
     */
    public String getNodeId() {
        return nodeId;
    }

    /**
     * The head branch name the changes are on.
     *
     * @return head branch name
     */
    public String getHeadBranch() {
        return headBranch;
    }

    /**
     * Gets the HEAD SHA.
     *
     * @return sha for the HEAD commit
     */
    public String getHeadSha() {
        return headSha;
    }

    /**
     * Gets status of the check suite. It can be one of request, in_progress, or completed.
     *
     * @return status of the check suite
     */
    public String getStatus() {
        return status;
    }

    /**
     * Gets conclusion of a completed check suite. It can be one of success, failure, neutral, cancelled, time_out,
     * action_required, or stale. The check suite will report the highest priority check run conclusion in the check
     * suite's conclusion.
     *
     * @return conclusion of the check suite
     */
    public String getConclusion() {
        return conclusion;
    }

    /**
     * The SHA of the most recent commit on ref before the push.
     *
     * @return sha of a commit
     */
    public String getBefore() {
        return before;
    }

    /**
     * The SHA of the most recent commit on ref after the push.
     *
     * @return sha of a commit
     */
    public String getAfter() {
        return after;
    }

    /**
     * The quantity of check runs that had run as part of the latest push.
     *
     * @return sha of the most recent commit
     */
    public int getLatestCheckRunsCount() {
        return latestCheckRunsCount;
    }

    /**
     * The url used to list all the check runs belonged to this suite.
     *
     * @return url containing all check runs
     */
    public URL getCheckRunsUrl() {
        return GitHubClient.parseURL(checkRunsUrl);
    }

    /**
     * The commit of current head.
     *
     * @return head commit
     */
    public HeadCommit getHeadCommit() {
        return headCommit;
    }

    /**
     * Gets the GitHub app this check suite belongs to, included in response.
     *
     * @return GitHub App
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHApp getApp() {
        return app;
    }

    /**
     * Gets the pull requests participated in this check suite.
     *
     * Note this field is only populated for events. When getting a {@link GHCheckSuite} outside of an event, this is
     * always empty.
     *
     * @return the list of {@link GHPullRequest}s for this check suite. Only populated for events.
     * @throws IOException
     *             the io exception
     */
    public List<GHPullRequest> getPullRequests() throws IOException {
        if (pullRequests != null && pullRequests.length != 0) {
            for (GHPullRequest singlePull : pullRequests) {
                // Only refresh if we haven't do so before
                singlePull.refresh(singlePull.getTitle());
            }
            return Collections.unmodifiableList(Arrays.asList(pullRequests));
        }
        return Collections.emptyList();
    }

    /**
     * Check suite doesn't have a HTML URL.
     *
     * @return null
     */
    @Override
    public URL getHtmlUrl() {
        return null;
    }

    /**
     * The Class HeadCommit.
     */
    public static class HeadCommit {
        private String id;
        private String treeId;
        private String message;
        private String timestamp;
        private GitUser author;
        private GitUser committer;

        /**
         * Gets id of the commit, used by {@link GHCheckSuite} when a {@link GHEvent#CHECK_SUITE} comes.
         *
         * @return id of the commit
         */
        public String getId() {
            return id;
        }

        /**
         * Gets id of the tree.
         *
         * @return id of the tree
         */
        public String getTreeId() {
            return treeId;
        }

        /**
         * Gets message.
         *
         * @return commit message.
         */
        public String getMessage() {
            return message;
        }

        /**
         * Gets timestamp of the commit.
         *
         * @return timestamp of the commit
         */
        public Date getTimestamp() {
            return GitHubClient.parseDate(timestamp);
        }

        /**
         * Gets author.
         *
         * @return the author
         */
        public GitUser getAuthor() {
            return author;
        }

        /**
         * Gets committer.
         *
         * @return the committer
         */
        public GitUser getCommitter() {
            return committer;
        }
    }
}
