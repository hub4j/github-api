package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHWorkflowRun.Conclusion;
import org.kohsuke.github.GHWorkflowRun.Status;
import org.kohsuke.github.function.InputStreamFunction;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * A workflow run job.
 *
 * @author Guillaume Smet
 */
public class GHWorkflowJob extends GHObject {

    // Not provided by the API.
    @JsonIgnore
    private GHRepository owner;

    private String name;

    private String headSha;

    private String startedAt;
    private String completedAt;

    private String status;
    private String conclusion;

    private long runId;
    private int runAttempt;

    private String htmlUrl;
    private String checkRunUrl;

    private int runnerId;
    private String runnerName;
    private int runnerGroupId;
    private String runnerGroupName;

    private List<Step> steps = new ArrayList<>();

    private List<String> labels = new ArrayList<>();

    /**
     * The name of the job.
     *
     * @return the name
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
     * When was this job started?
     *
     * @return start date
     */
    public Date getStartedAt() {
        return GitHubClient.parseDate(startedAt);
    }

    /**
     * When was this job completed?
     *
     * @return completion date
     */
    public Date getCompletedAt() {
        return GitHubClient.parseDate(completedAt);
    }

    /**
     * Gets status of the job.
     * <p>
     * Can be {@code UNKNOWN} if the value returned by GitHub is unknown from the API.
     *
     * @return status of the job
     */
    public Status getStatus() {
        return Status.from(status);
    }

    /**
     * Gets the conclusion of the job.
     * <p>
     * Can be {@code UNKNOWN} if the value returned by GitHub is unknown from the API.
     *
     * @return conclusion of the job
     */
    public Conclusion getConclusion() {
        return Conclusion.from(conclusion);
    }

    /**
     * The run id.
     *
     * @return the run id
     */
    public long getRunId() {
        return runId;
    }

    /**
     * Attempt number of the associated workflow run, 1 for first attempt and higher if the workflow was re-run.
     *
     * @return attempt number
     */
    public int getRunAttempt() {
        return runAttempt;
    }

    @Override
    public URL getHtmlUrl() {
        return GitHubClient.parseURL(htmlUrl);
    }

    /**
     * The check run URL.
     *
     * @return the check run url
     */
    public URL getCheckRunUrl() {
        return GitHubClient.parseURL(checkRunUrl);
    }

    /**
     * Gets the execution steps of this job.
     *
     * @return the execution steps
     */
    public List<Step> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    /**
     * Gets the labels of the job.
     *
     * @return the labels
     */
    public List<String> getLabels() {
        return Collections.unmodifiableList(labels);
    }

    /**
     * the runner id.
     *
     * @return runnerId
     */
    public int getRunnerId() {
        return runnerId;
    }

    /**
     * the runner name.
     *
     * @return runnerName
     */
    public String getRunnerName() {
        return runnerName;
    }

    /**
     * the runner group id.
     *
     * @return runnerGroupId
     */
    public int getRunnerGroupId() {
        return runnerGroupId;
    }

    /**
     * the runner group name.
     *
     * @return runnerGroupName
     */
    public String getRunnerGroupName() {
        return runnerGroupName;
    }

    /**
     * Repository to which the job belongs.
     *
     * @return the repository
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHRepository getRepository() {
        return owner;
    }

    /**
     * Downloads the logs.
     * <p>
     * The logs are returned as a text file.
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

        return root().createRequest().method("GET").withUrlPath(getApiRoute(), "logs").fetchStream(streamFunction);
    }

    private String getApiRoute() {
        if (owner == null) {
            // Workflow runs returned from search to do not have an owner. Attempt to use url.
            final URL url = Objects.requireNonNull(getUrl(), "Missing instance URL!");
            return StringUtils.prependIfMissing(url.toString().replace(root().getApiUrl(), ""), "/");

        }
        return "/repos/" + owner.getOwnerName() + "/" + owner.getName() + "/actions/jobs/" + getId();
    }

    GHWorkflowJob wrapUp(GHRepository owner) {
        this.owner = owner;
        return this;
    }

    public static class Step {

        private String name;
        private int number;

        private String startedAt;
        private String completedAt;

        private String status;
        private String conclusion;

        /**
         * Gets the name of the step.
         *
         * @return name
         */
        public String getName() {
            return name;
        }

        /**
         * Gets the sequential number of the step.
         *
         * @return number
         */
        public int getNumber() {
            return number;
        }

        /**
         * When was this step started?
         *
         * @return start date
         */
        public Date getStartedAt() {
            return GitHubClient.parseDate(startedAt);
        }

        /**
         * When was this step completed?
         *
         * @return completion date
         */
        public Date getCompletedAt() {
            return GitHubClient.parseDate(completedAt);
        }

        /**
         * Gets status of the step.
         * <p>
         * Can be {@code UNKNOWN} if the value returned by GitHub is unknown from the API.
         *
         * @return status of the step
         */
        public Status getStatus() {
            return Status.from(status);
        }

        /**
         * Gets the conclusion of the step.
         * <p>
         * Can be {@code UNKNOWN} if the value returned by GitHub is unknown from the API.
         *
         * @return conclusion of the step
         */
        public Conclusion getConclusion() {
            return Conclusion.from(conclusion);
        }
    }
}
