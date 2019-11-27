package org.kohsuke.github;

import java.io.IOException;
import java.net.URL;

/**
 * Represents a deployment
 *
 * @see <a href="https://developer.github.com/v3/checks/runs/">documentation</a>
 */

public class GHCheckRun extends GHObject {
    private GHRepository owner;
    private GitHub root;
    private String status;
    private String conclusion;
    private String name;
    private GHPullRequest[] pullRequests;

    GHCheckRun wrap(GHRepository owner) {
        this.owner = owner;
        wrap(owner.root);
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
        for (GHPullRequest singlePull : pullRequests) {
            singlePull.wrap(owner);
        }
        return pullRequests;
    }

    String getStatus() {
        return status;
    }

    String getConclusion() {
        return conclusion;
    }

    String getName() {
        return name;
    }

    GHPullRequest[] getPullRequests() throws IOException {
        for (GHPullRequest singlePull : pullRequests) {
            singlePull.refresh();
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
