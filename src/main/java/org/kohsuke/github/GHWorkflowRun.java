package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.function.InputStreamFunction;
import org.kohsuke.github.internal.EnumUtils;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

// TODO: Auto-generated Javadoc
/**
 * A workflow run.
 *
 * @author Guillaume Smet
 * @see GHRepository#getWorkflowRun(long)
 */
public class GHWorkflowRun extends GHObject {

    /**
     * The Enum Conclusion.
     */
    public static enum Conclusion {

        /** The action required. */
        ACTION_REQUIRED,
        /** The cancelled. */
        CANCELLED,
        /** The failure. */
        FAILURE,
        /** The neutral. */
        NEUTRAL,
        /** The skipped. */
        SKIPPED,
        /** The stale. */
        STALE,
        /** Start up fail */
        STARTUP_FAILURE,
        /** The success. */
        SUCCESS,
        /** The timed out. */
        TIMED_OUT,
        /** The unknown. */
        UNKNOWN;

        /**
         * From.
         *
         * @param value
         *            the value
         * @return the conclusion
         */
        public static Conclusion from(String value) {
            return EnumUtils.getNullableEnumOrDefault(Conclusion.class, value, Conclusion.UNKNOWN);
        }

        /**
         * To string.
         *
         * @return the string
         */
        @Override
        public String toString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    /**
     * The Class HeadCommit.
     */
    public static class HeadCommit extends GitHubBridgeAdapterObject {

        private GitUser author;

        private GitUser committer;
        private String id;
        private String message;
        private String timestamp;
        private String treeId;
        /**
         * Create default HeadCommit instance
         */
        public HeadCommit() {
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

        /**
         * Gets id of the commit.
         *
         * @return id of the commit
         */
        public String getId() {
            return id;
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
        @WithBridgeMethods(value = Date.class, adapterMethod = "instantToDate")
        public Instant getTimestamp() {
            return GitHubClient.parseInstant(timestamp);
        }

        /**
         * Gets id of the tree.
         *
         * @return id of the tree
         */
        public String getTreeId() {
            return treeId;
        }
    }

    /**
     * The Enum Status.
     */
    public static enum Status {

        /** The action required. */
        ACTION_REQUIRED,
        /** The cancelled. */
        CANCELLED,
        /** The completed. */
        COMPLETED,
        /** The failure. */
        FAILURE,
        /** The in progress. */
        IN_PROGRESS,
        /** The neutral. */
        NEUTRAL,
        /** The pending. */
        PENDING,
        /** The queued. */
        QUEUED,
        /** The requested. */
        REQUESTED,
        /** The skipped. */
        SKIPPED,
        /** The stale. */
        STALE,
        /** The success. */
        SUCCESS,
        /** The timed out. */
        TIMED_OUT,
        /** The unknown. */
        UNKNOWN,
        /** The waiting. */
        WAITING;

        /**
         * From.
         *
         * @param value
         *            the value
         * @return the status
         */
        public static Status from(String value) {
            return EnumUtils.getNullableEnumOrDefault(Status.class, value, Status.UNKNOWN);
        }

        /**
         * To string.
         *
         * @return the string
         */
        @Override
        public String toString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
    private GHUser actor;
    private String artifactsUrl;
    private String cancelUrl;
    private String checkSuiteUrl;
    private String conclusion;

    private String displayTitle;
    private String event;

    private String headBranch;
    private HeadCommit headCommit;
    private GHRepository headRepository;
    private String headSha;
    private String htmlUrl;
    private String jobsUrl;
    private String logsUrl;
    private String name;

    @JsonProperty("repository")
    private GHRepository owner;
    private GHPullRequest[] pullRequests;
    private String rerunUrl;
    private long runAttempt;

    private long runNumber;
    private String runStartedAt;
    private String status;

    private GHUser triggeringActor;

    private long workflowId;

    private String workflowUrl;

    /**
     * Create default GHWorkflowRun instance
     */
    public GHWorkflowRun() {
    }

    /**
     * Approve the workflow run.
     *
     * @throws IOException
     *             the io exception
     */
    public void approve() throws IOException {
        root().createRequest().method("POST").withUrlPath(getApiRoute(), "approve").send();
    }

    /**
     * Cancel the workflow run.
     *
     * @throws IOException
     *             the io exception
     */
    public void cancel() throws IOException {
        root().createRequest().method("POST").withUrlPath(getApiRoute(), "cancel").send();
    }

    /**
     * Delete the workflow run.
     *
     * @throws IOException
     *             the io exception
     */
    public void delete() throws IOException {
        root().createRequest().method("DELETE").withUrlPath(getApiRoute()).send();
    }

    /**
     * Delete the logs.
     *
     * @throws IOException
     *             the io exception
     */
    public void deleteLogs() throws IOException {
        root().createRequest().method("DELETE").withUrlPath(getApiRoute(), "logs").send();
    }

    /**
     * Downloads the logs.
     * <p>
     * The logs are in the form of a zip archive.
     * <p>
     * Note that the archive is the same as the one downloaded from a workflow run so it contains the logs for all jobs.
     *
     * @param <T>
     *            the type of result
     * @param streamFunction
     *            The {@link InputStreamFunction} that will process the stream
     * @return the result of reading the stream.
     * @throws IOException
     *             The IO exception.
     */
    public <T> T downloadLogs(InputStreamFunction<T> streamFunction) throws IOException {
        requireNonNull(streamFunction, "Stream function must not be null");

        return root().createRequest().method("GET").withUrlPath(getApiRoute(), "logs").fetchStream(streamFunction);
    }

    /**
     * Force-cancel the workflow run.
     *
     * @throws IOException
     *             the io exception
     */
    public void forceCancel() throws IOException {
        root().createRequest().method("POST").withUrlPath(getApiRoute(), "force-cancel").send();
    }

    /**
     * The actor which triggered the initial run.
     *
     * @return the triggering actor
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHUser getActor() {
        return actor;
    }

    /**
     * The artifacts URL, like https://api.github.com/repos/octo-org/octo-repo/actions/runs/30433642/artifacts
     *
     * @return the artifacts url
     */
    public URL getArtifactsUrl() {
        return GitHubClient.parseURL(artifactsUrl);
    }

    /**
     * The cancel URL, like https://api.github.com/repos/octo-org/octo-repo/actions/runs/30433642/cancel
     *
     * @return the cancel url
     */
    public URL getCancelUrl() {
        return GitHubClient.parseURL(cancelUrl);
    }

    /**
     * The check suite URL, like https://api.github.com/repos/octo-org/octo-repo/check-suites/414944374
     *
     * @return the check suite url
     */
    public URL getCheckSuiteUrl() {
        return GitHubClient.parseURL(checkSuiteUrl);
    }

    /**
     * Gets the conclusion of the workflow run.
     * <p>
     * Can be {@code UNKNOWN} if the value returned by GitHub is unknown from the API.
     *
     * @return conclusion of the workflow run
     */
    public Conclusion getConclusion() {
        return Conclusion.from(conclusion);
    }

    /**
     * The display title of the workflow run.
     *
     * @return the displayTitle
     */
    public String getDisplayTitle() {
        return displayTitle;
    }

    /**
     * The type of event that triggered the build.
     *
     * @return type of event
     */
    public GHEvent getEvent() {
        return EnumUtils.getNullableEnumOrDefault(GHEvent.class, event, GHEvent.UNKNOWN);
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
     * The commit of current head.
     *
     * @return head commit
     */
    public HeadCommit getHeadCommit() {
        return headCommit;
    }

    /**
     * The repository of current head.
     *
     * @return head repository
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHRepository getHeadRepository() {
        return headRepository;
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
     * Gets the html url.
     *
     * @return the html url
     */
    public URL getHtmlUrl() {
        return GitHubClient.parseURL(htmlUrl);
    }

    /**
     * The jobs URL, like https://api.github.com/repos/octo-org/octo-repo/actions/runs/30433642/jobs
     *
     * @return the jobs url
     */
    public URL getJobsUrl() {
        return GitHubClient.parseURL(jobsUrl);
    }

    /**
     * The logs URL, like https://api.github.com/repos/octo-org/octo-repo/actions/runs/30433642/logs
     *
     * @return the logs url
     */
    public URL getLogsUrl() {
        return GitHubClient.parseURL(logsUrl);
    }

    /**
     * The name of the workflow run.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the pull requests participated in this workflow run.
     *
     * Note this field is only populated for events. When getting a {@link GHWorkflowRun} outside of an event, this is
     * always empty.
     *
     * @return the list of {@link GHPullRequest}s for this workflow run. Only populated for events.
     * @throws IOException
     *             the io exception
     */
    public List<GHPullRequest> getPullRequests() throws IOException {
        if (pullRequests != null && pullRequests.length != 0) {
            for (GHPullRequest pullRequest : pullRequests) {
                // Only refresh if we haven't do so before
                pullRequest.refresh(pullRequest.getTitle());
            }
            return Collections.unmodifiableList(Arrays.asList(pullRequests));
        }
        return Collections.emptyList();
    }

    /**
     * Repository to which the workflow run belongs.
     *
     * @return the repository
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHRepository getRepository() {
        return owner;
    }

    /**
     * The rerun URL, like https://api.github.com/repos/octo-org/octo-repo/actions/runs/30433642/rerun
     *
     * @return the rerun url
     */
    public URL getRerunUrl() {
        return GitHubClient.parseURL(rerunUrl);
    }

    /**
     * The run attempt.
     *
     * @return the run attempt
     */
    public long getRunAttempt() {
        return runAttempt;
    }

    /**
     * The run number.
     *
     * @return the run number
     */
    public long getRunNumber() {
        return runNumber;
    }

    /**
     * When was this run triggered?.
     *
     * @return run triggered
     */
    @WithBridgeMethods(value = Date.class, adapterMethod = "instantToDate")
    public Instant getRunStartedAt() {
        return GitHubClient.parseInstant(runStartedAt);
    }

    /**
     * Gets status of the workflow run.
     * <p>
     * Can be {@code UNKNOWN} if the value returned by GitHub is unknown from the API.
     *
     * @return status of the workflow run
     */
    public Status getStatus() {
        return Status.from(status);
    }

    /**
     * The actor which triggered the run.
     *
     * @return the triggering actor
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHUser getTriggeringActor() {
        return triggeringActor;
    }

    /**
     * The workflow id.
     *
     * @return the workflow id
     */
    public long getWorkflowId() {
        return workflowId;
    }

    /**
     * The workflow URL, like https://api.github.com/repos/octo-org/octo-repo/actions/workflows/159038
     *
     * @return the workflow url
     */
    public URL getWorkflowUrl() {
        return GitHubClient.parseURL(workflowUrl);
    }

    /**
     * Returns the list of jobs from all the executions of this workflow run.
     *
     * @return list of jobs from all the executions
     */
    public PagedIterable<GHWorkflowJob> listAllJobs() {
        return new GHWorkflowJobQueryBuilder(this).all().list();
    }

    /**
     * Lists the artifacts attached to this workflow run.
     *
     * @return the paged iterable
     */
    public PagedIterable<GHArtifact> listArtifacts() {
        return new GHArtifactsIterable(owner, root().createRequest().withUrlPath(getApiRoute(), "artifacts"));
    }

    /**
     * Returns the list of jobs of this workflow run for the last execution.
     *
     * @return list of jobs from the last execution
     */
    public PagedIterable<GHWorkflowJob> listJobs() {
        return new GHWorkflowJobQueryBuilder(this).latest().list();
    }

    /**
     * Rerun the workflow run.
     *
     * @throws IOException
     *             the io exception
     */
    public void rerun() throws IOException {
        root().createRequest().method("POST").withUrlPath(getApiRoute(), "rerun").send();
    }

    private String getApiRoute() {
        if (owner == null) {
            // Workflow runs returned from search to do not have an owner. Attempt to use url.
            final URL url = Objects.requireNonNull(getUrl(), "Missing instance URL!");
            return StringUtils.prependIfMissing(url.toString().replace(root().getApiUrl(), ""), "/");

        }
        return "/repos/" + owner.getOwnerName() + "/" + owner.getName() + "/actions/runs/" + getId();
    }

    /**
     * Wrap up.
     *
     * @param owner
     *            the owner
     * @return the GH workflow run
     */
    GHWorkflowRun wrapUp(GHRepository owner) {
        this.owner = owner;
        return wrapUp(owner.root());
    }

    /**
     * Wrap up.
     *
     * @param root
     *            the root
     * @return the GH workflow run
     */
    GHWorkflowRun wrapUp(GitHub root) {
        if (owner != null) {
            if (pullRequests != null) {
                for (GHPullRequest singlePull : pullRequests) {
                    singlePull.wrap(owner);
                }
            }
        }
        return this;
    }
}
