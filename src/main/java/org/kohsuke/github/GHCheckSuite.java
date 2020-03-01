package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.net.URL;

/**
 * Represents a check suite.
 *
 * @see <a href="https://developer.github.com/v3/checks/suites/">documentation</a>
 */
@SuppressFBWarnings(value = { "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD", "URF_UNREAD_FIELD" },
        justification = "JSON API")
public class GHCheckSuite extends GHObject {
    GHRepository owner;
    GitHub root;

    private String nodeId;
    private String headBranch;
    private String headSha;
    private String status;
    private String conclusion;
    private String before;
    private String after;
    private int latestCheckRunsCount;
    private URL checkRunsUrl;
    private GHCommit.ShortInfo headCommit;
    private GHApp app;
    private GHPullRequest[] pullRequests;

    GHCheckSuite wrap(GHRepository owner) {
        this.owner = owner;
        this.root = owner.root;
        return this;
    }

    GHCheckSuite wrap(GitHub root) {
        this.root = root;
        if (owner != null) {
            owner.wrap(root);
        }
        return this;
    }

    GHPullRequest[] wrap() {
        return pullRequests;
    }

    /**
     * Gets the global node id to access most objects in GitHub.
     *
     * @see <a href="https://developer.github.com/v4/guides/using-global-node-ids/">documentation</a>
     * @return global node id
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
        return checkRunsUrl;
    }

    /**
     * The commit of current head.
     *
     * @return head commit
     */
    public GHCommit.ShortInfo getHeadCommit() {
        return headCommit;
    }

    /**
     * Gets the GitHub app this check suite belongs to, included in response.
     *
     * @return GitHub App
     */
    public GHApp getApp() {
        return app;
    }

    /**
     * Gets the pull requests participated in this check suite.
     *
     * @return Pull requests
     */
    GHPullRequest[] getPullRequests() throws IOException {
        if (pullRequests != null && pullRequests.length != 0) {
            for (GHPullRequest singlePull : pullRequests) {
                singlePull.refresh();
            }
        }
        return pullRequests;
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
}
