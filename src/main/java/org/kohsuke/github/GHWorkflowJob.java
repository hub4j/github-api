package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHWorkflowRun.Conclusion;
import org.kohsuke.github.GHWorkflowRun.Status;
import org.kohsuke.github.function.InputStreamFunction;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

// TODO: Auto-generated Javadoc
/**
 * A workflow run job.
 *
 * @author Guillaume Smet
 */
public class GHWorkflowJob extends GHObject {

    /**
     * The Class Step.
     */
    public static class Step extends GitHubBridgeAdapterObject {

        private String completedAt;

        private String conclusion;
        private String name;

        private int number;
        private String startedAt;

        private String status;
        /**
         * Create default Step instance
         */
        public Step() {
        }

        /**
         * When was this step completed?.
         *
         * @return completion date
         */
        @WithBridgeMethods(value = Date.class, adapterMethod = "instantToDate")
        public Instant getCompletedAt() {
            return GitHubClient.parseInstant(completedAt);
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
         * When was this step started?.
         *
         * @return start date
         */
        @WithBridgeMethods(value = Date.class, adapterMethod = "instantToDate")
        public Instant getStartedAt() {
            return GitHubClient.parseInstant(startedAt);
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
    }

    private String checkRunUrl;

    private String completedAt;

    private String conclusion;

    private String headSha;
    private String htmlUrl;

    private List<String> labels = new ArrayList<>();
    private String name;

    // Not provided by the API.
    @JsonIgnore
    private GHRepository owner;
    private int runAttempt;

    private long runId;
    private int runnerGroupId;

    private String runnerGroupName;
    private int runnerId;
    private String runnerName;
    private String startedAt;

    private String status;

    private List<Step> steps = new ArrayList<>();

    /**
     * Create default GHWorkflowJob instance
     */
    public GHWorkflowJob() {
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
     * @return the result of reading the stream.
     * @throws IOException
     *             The IO exception.
     */
    public <T> T downloadLogs(InputStreamFunction<T> streamFunction) throws IOException {
        requireNonNull(streamFunction, "Stream function must not be null");

        return root().createRequest().method("GET").withUrlPath(getApiRoute(), "logs").fetchStream(streamFunction);
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
     * When was this job completed?.
     *
     * @return completion date
     */
    @WithBridgeMethods(value = Date.class, adapterMethod = "instantToDate")
    public Instant getCompletedAt() {
        return GitHubClient.parseInstant(completedAt);
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
     * Gets the labels of the job.
     *
     * @return the labels
     */
    public List<String> getLabels() {
        return Collections.unmodifiableList(labels);
    }

    /**
     * The name of the job.
     *
     * @return the name
     */
    public String getName() {
        return name;
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
     * Attempt number of the associated workflow run, 1 for first attempt and higher if the workflow was re-run.
     *
     * @return attempt number
     */
    public int getRunAttempt() {
        return runAttempt;
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
     * When was this job started?.
     *
     * @return start date
     */
    @WithBridgeMethods(value = Date.class, adapterMethod = "instantToDate")
    public Instant getStartedAt() {
        return GitHubClient.parseInstant(startedAt);
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
     * Gets the execution steps of this job.
     *
     * @return the execution steps
     */
    public List<Step> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    private String getApiRoute() {
        if (owner == null) {
            // Workflow runs returned from search to do not have an owner. Attempt to use url.
            final URL url = Objects.requireNonNull(getUrl(), "Missing instance URL!");
            return StringUtils.prependIfMissing(url.toString().replace(root().getApiUrl(), ""), "/");

        }
        return "/repos/" + owner.getOwnerName() + "/" + owner.getName() + "/actions/jobs/" + getId();
    }

    /**
     * Wrap up.
     *
     * @param owner
     *            the owner
     * @return the GH workflow job
     */
    GHWorkflowJob wrapUp(GHRepository owner) {
        this.owner = owner;
        return this;
    }
}
