package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.net.URL;

/**
 * Represents a check run.
 *
 * @see <a href="https://developer.github.com/v3/checks/runs/">documentation</a>
 */

@SuppressFBWarnings(value = { "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD", "URF_UNREAD_FIELD" },
        justification = "JSON API")
public class GHCheckRun extends GHObject {
    GHRepository owner;
    GitHub root;

    private String status;
    private String conclusion;
    private String name;
    private String headSha;
    private GHPullRequest[] pullRequests;

    GHCheckRun wrap(GHRepository owner) {
        this.owner = owner;
        this.root = owner.root;
        return this;
    }

    GHCheckRun wrap(GitHub root) {
        this.root = root;
        if (owner != null) {
            owner.wrap(root);
        }
        return this;
    }

    GHPullRequest[] wrap() {
        return pullRequests;
    }

    public String getStatus() {
        return status;
    }

    public String getConclusion() {
        return conclusion;
    }

    public String getName() {
        return name;
    }

    /**
     * Gets the HEAD SHA.
     *
     * @return sha for the HEAD commit
     */
    public String getHeadSha() {
        return headSha;
    }

    GHPullRequest[] getPullRequests() throws IOException {
        if (pullRequests != null && pullRequests.length != 0) {
            for (GHPullRequest singlePull : pullRequests) {
                singlePull.refresh();
            }
        }
        return pullRequests;
    }

    /**
     * @deprecated This object has no HTML URL.
     */
    @Override
    public URL getHtmlUrl() {
        return null;
    }

}
