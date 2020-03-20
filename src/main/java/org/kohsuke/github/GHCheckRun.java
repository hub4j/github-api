package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.net.URL;
import java.util.Date;

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
    private String nodeId;
    private String externalId;
    private String startedAt;
    private String completedAt;
    private URL htmlUrl;
    private URL detailsUrl;
    private Output output;
    private GHApp app;
    private GHPullRequest[] pullRequests;
    private GHCheckSuite checkSuite;

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

    /**
     * Gets status of the check run. It can be one of "queue", "in_progress", or "completed"
     *
     * @return Status of the check run
     */
    public String getStatus() {
        return status;
    }

    /**
     * Gets conclusion of a completed check run. It can be one of "success", "failure", "neutral", "cancelled",
     * "time_out", or "action_required".
     *
     * @return Status of the check run
     */
    public String getConclusion() {
        return conclusion;
    }

    /**
     * Gets the custom name of this check run.
     *
     * @return Name of the check run
     */
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

    /**
     * Gets the pull requests participated in this check run.
     *
     * @return Pull requests of this check run
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
     * Gets the HTML URL: https://github.com/[owner]/[repo-name]/runs/[check-run-id], usually an GitHub Action page of
     * the check run.
     *
     * @return HTML URL
     */
    @Override
    public URL getHtmlUrl() {
        return htmlUrl;
    }

    /**
     * Gets the global node id to access most objects in GitHub.
     *
     * @see <a href="https://developer.github.com/v4/guides/using-global-node-ids/">documentation</a>
     * @return Global node id
     */
    public String getNodeId() {
        return nodeId;
    }

    /**
     * Gets a reference for the check run on the integrator's system.
     *
     * @return Reference id
     */
    public String getExternalId() {
        return externalId;
    }

    /**
     * Gets the details URL from which to find full details of the check run on the integrator's site.
     *
     * @return Details URL
     */
    public URL getDetailsUrl() {
        return detailsUrl;
    }

    /**
     * Gets the start time of the check run in ISO 8601 format: YYYY-MM-DDTHH:MM:SSZ.
     *
     * @return Timestamp of the start time
     */
    public Date getStartedAt() {
        return GitHubClient.parseDate(startedAt);
    }

    /**
     * Gets the completed time of the check run in ISO 8601 format: YYYY-MM-DDTHH:MM:SSZ.
     *
     * @return Timestamp of the completed time
     */
    public Date getCompletedAt() {
        return GitHubClient.parseDate(completedAt);
    }

    /**
     * Gets the GitHub app this check run belongs to, included in response.
     *
     * @return GitHub App
     */
    public GHApp getApp() {
        return app;
    }

    /**
     * Gets the check suite this check run belongs to
     *
     * @return Check suite
     */
    public GHCheckSuite getCheckSuite() {
        return checkSuite;
    }

    /**
     * Gets an output for a check run.
     *
     * @return Output of a check run
     */
    public Output getOutput() {
        return output;
    }

    /**
     * Represents an output in a check run to include summary and other results.
     *
     * @see <a href="https://developer.github.com/v3/checks/runs/#output-object">documentation</a>
     */
    public static class Output {
        private String title;
        private String summary;
        private String text;
        private int annotationsCount;
        private URL annotationsUrl;

        /**
         * Gets the title of check run.
         *
         * @return title of check run
         */
        public String getTitle() {
            return title;
        }

        /**
         * Gets the summary of the check run, note that it supports Markdown.
         *
         * @return summary of check run
         */
        public String getSummary() {
            return summary;
        }

        /**
         * Gets the details of the check run, note that it supports Markdown.
         *
         * @return Details of the check run
         */
        public String getText() {
            return text;
        }

        /**
         * Gets the annotation count of a check run.
         *
         * @return annotation count of a check run
         */
        public int getAnnotationsCount() {
            return annotationsCount;
        }

        /**
         * Gets the URL of annotations.
         *
         * @return URL of annotations
         */
        public URL getAnnotationsUrl() {
            return annotationsUrl;
        }
    }

}
