package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.function.InputStreamFunction;
import org.kohsuke.github.internal.EnumUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * A workflow run.
 *
 * @author Guillaume Smet
 * @see GHRepository#getWorkflowRun(long)
 */
public class GHWorkflowRun extends GHObject {

    @JsonProperty("repository")
    private GHRepository owner;

    private String name;
    private long runNumber;
    private long workflowId;

    private String htmlUrl;
    private String jobsUrl;
    private String logsUrl;
    private String checkSuiteUrl;
    private String artifactsUrl;
    private String cancelUrl;
    private String rerunUrl;
    private String workflowUrl;

    private String headBranch;
    private String headSha;
    private GHRepository headRepository;
    private HeadCommit headCommit;

    private String event;
    private String status;
    private String conclusion;

    private GHPullRequest[] pullRequests;

    /**
     * The name of the workflow run.
     *
     * @return the name
     */
    public String getName() {
        return name;
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
     * The workflow id.
     *
     * @return the workflow id
     */
    public long getWorkflowId() {
        return workflowId;
    }

    @Override
    public URL getHtmlUrl() throws IOException {
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
     * The check suite URL, like https://api.github.com/repos/octo-org/octo-repo/check-suites/414944374
     *
     * @return the check suite url
     */
    public URL getCheckSuiteUrl() {
        return GitHubClient.parseURL(checkSuiteUrl);
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
     * The rerun URL, like https://api.github.com/repos/octo-org/octo-repo/actions/runs/30433642/rerun
     *
     * @return the rerun url
     */
    public URL getRerunUrl() {
        return GitHubClient.parseURL(rerunUrl);
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
    public GHRepository getHeadRepository() {
        return headRepository;
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
     * Repository to which the workflow run belongs.
     *
     * @return the repository
     */
    public GHRepository getRepository() {
        return owner;
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
     * Cancel the workflow run.
     *
     * @throws IOException
     *             the io exception
     */
    public void cancel() throws IOException {
        root.createRequest().method("POST").withUrlPath(getApiRoute(), "cancel").fetchHttpStatusCode();
    }

    /**
     * Delete the workflow run.
     *
     * @throws IOException
     *             the io exception
     */
    public void delete() throws IOException {
        root.createRequest().method("DELETE").withUrlPath(getApiRoute()).fetchHttpStatusCode();
    }

    /**
     * Rerun the workflow run.
     *
     * @throws IOException
     *             the io exception
     */
    public void rerun() throws IOException {
        root.createRequest().method("POST").withUrlPath(getApiRoute(), "rerun").fetchHttpStatusCode();
    }

    /**
     * Lists the artifacts attached to this workflow run.
     *
     * @return the paged iterable
     */
    public PagedIterable<GHArtifact> listArtifacts() {
        return new GHArtifactsIterable(owner, root.createRequest().withUrlPath(getApiRoute(), "artifacts"));
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
     * @throws IOException
     *             The IO exception.
     * @return the result of reading the stream.
     */
    public <T> T downloadLogs(InputStreamFunction<T> streamFunction) throws IOException {
        requireNonNull(streamFunction, "Stream function must not be null");

        return root.createRequest().method("GET").withUrlPath(getApiRoute(), "logs").fetchStream(streamFunction);
    }

    /**
     * Delete the logs.
     *
     * @throws IOException
     *             the io exception
     */
    public void deleteLogs() throws IOException {
        root.createRequest().method("DELETE").withUrlPath(getApiRoute(), "logs").fetchHttpStatusCode();
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
     * Returns the list of jobs from all the executions of this workflow run.
     *
     * @return list of jobs from all the executions
     */
    public PagedIterable<GHWorkflowJob> listAllJobs() {
        return new GHWorkflowJobQueryBuilder(this).all().list();
    }

    private String getApiRoute() {
        if (owner == null) {
            // Workflow runs returned from search to do not have an owner. Attempt to use url.
            final URL url = Objects.requireNonNull(getUrl(), "Missing instance URL!");
            return StringUtils.prependIfMissing(url.toString().replace(root.getApiUrl(), ""), "/");

        }
        return "/repos/" + owner.getOwnerName() + "/" + owner.getName() + "/actions/runs/" + getId();
    }

    GHWorkflowRun wrapUp(GHRepository owner) {
        this.owner = owner;
        return wrapUp(owner.root);
    }

    GHWorkflowRun wrapUp(GitHub root) {
        this.root = root;
        if (owner != null) {
            owner.wrap(root);
            if (pullRequests != null) {
                for (GHPullRequest singlePull : pullRequests) {
                    singlePull.wrap(owner);
                }
            }
        } else if (pullRequests != null) {
            for (GHPullRequest singlePull : pullRequests) {
                singlePull.wrap(root);
            }
        }
        if (headRepository != null) {
            headRepository.wrap(root);
        }
        return this;
    }

    public static class HeadCommit {
        private String id;
        private String treeId;
        private String message;
        private String timestamp;
        private GitUser author;
        private GitUser committer;

        /**
         * Gets id of the commit
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

    public static enum Status {
        QUEUED, IN_PROGRESS, COMPLETED, UNKNOWN;

        public static Status from(String value) {
            return EnumUtils.getNullableEnumOrDefault(Status.class, value, Status.UNKNOWN);
        }

        @Override
        public String toString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public static enum Conclusion {
        ACTION_REQUIRED, CANCELLED, FAILURE, NEUTRAL, SUCCESS, SKIPPED, STALE, TIMED_OUT, UNKNOWN;

        public static Conclusion from(String value) {
            return EnumUtils.getNullableEnumOrDefault(Conclusion.class, value, Conclusion.UNKNOWN);
        }

        @Override
        public String toString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
