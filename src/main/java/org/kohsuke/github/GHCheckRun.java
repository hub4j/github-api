package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.kohsuke.github.internal.EnumUtils;
import org.kohsuke.github.internal.Previews;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// TODO: Auto-generated Javadoc
/**
 * Represents a check run.
 *
 * @see <a href="https://developer.github.com/v3/checks/runs/">documentation</a>
 */
@SuppressFBWarnings(value = { "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD", "URF_UNREAD_FIELD" },
        justification = "JSON API")
public class GHCheckRun extends GHObject {

    /** The owner. */
    @JsonProperty("repository")
    GHRepository owner;

    private String status;
    private String conclusion;
    private String name;
    private String headSha;
    private String nodeId;
    private String externalId;
    private String startedAt;
    private String completedAt;
    private String htmlUrl;
    private String detailsUrl;
    private Output output;
    private GHApp app;
    private GHPullRequest[] pullRequests = new GHPullRequest[0];
    private GHCheckSuite checkSuite;

    /**
     * Wrap.
     *
     * @param owner the owner
     * @return the GH check run
     */
    GHCheckRun wrap(GHRepository owner) {
        this.owner = owner;
        wrap(owner.root());
        return this;
    }

    /**
     * Wrap.
     *
     * @param root the root
     * @return the GH check run
     */
    GHCheckRun wrap(GitHub root) {
        if (owner != null) {
            for (GHPullRequest singlePull : pullRequests) {
                singlePull.wrap(owner);
            }
        }
        if (checkSuite != null) {
            if (owner != null) {
                checkSuite.wrap(owner);
            } else {
                checkSuite.wrap(root);
            }
        }

        return this;
    }

    /**
     * Gets status of the check run.
     *
     * @return Status of the check run
     * @see Status
     */
    @WithBridgeMethods(value = String.class, adapterMethod = "statusAsStr")
    public Status getStatus() {
        return Status.from(status);
    }

    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "Bridge method of getStatus")
    private Object statusAsStr(Status status, Class type) {
        return status;
    }

    /**
     * The Enum Status.
     */
    public static enum Status {
        
        /** The queued. */
        QUEUED, 
 /** The in progress. */
 IN_PROGRESS, 
 /** The completed. */
 COMPLETED, 
 /** The unknown. */
 UNKNOWN;

        /**
         * From.
         *
         * @param value the value
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

    /**
     * Gets conclusion of a completed check run.
     *
     * @return Status of the check run
     * @see Conclusion
     */
    @WithBridgeMethods(value = String.class, adapterMethod = "conclusionAsStr")
    public Conclusion getConclusion() {
        return Conclusion.from(conclusion);
    }

    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "Bridge method of getConclusion")
    private Object conclusionAsStr(Conclusion conclusion, Class type) {
        return conclusion;
    }

    /**
     * Final conclusion of the check.
     *
     * From <a href="https://docs.github.com/en/rest/reference/checks#create-a-check-run--parameters">Check Run
     * Parameters - <code>conclusion</code></a>.
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
 /** The success. */
 SUCCESS, 
 /** The skipped. */
 SKIPPED, 
 /** The stale. */
 STALE, 
 /** The timed out. */
 TIMED_OUT, 
 /** The unknown. */
 UNKNOWN;

        /**
         * From.
         *
         * @param value the value
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
     * Note this field is only populated for events. When getting a {@link GHCheckRun} outside of an event, this is
     * always empty.
     *
     * @return the list of {@link GHPullRequest}s for this check run. Only populated for events.
     * @throws IOException
     *             the io exception
     */
    public List<GHPullRequest> getPullRequests() throws IOException {
        for (GHPullRequest singlePull : pullRequests) {
            // Only refresh if we haven't do so before
            singlePull.refresh(singlePull.getTitle());
        }
        return Collections.unmodifiableList(Arrays.asList(pullRequests));
    }

    /**
     * Gets the HTML URL: https://github.com/[owner]/[repo-name]/runs/[check-run-id], usually an GitHub Action page of
     * the check run.
     *
     * @return HTML URL
     */
    @Override
    public URL getHtmlUrl() {
        return GitHubClient.parseURL(htmlUrl);
    }

    /**
     * Gets the global node id to access most objects in GitHub.
     *
     * @return Global node id
     * @see <a href="https://developer.github.com/v4/guides/using-global-node-ids/">documentation</a>
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
        return GitHubClient.parseURL(detailsUrl);
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
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
    public GHApp getApp() {
        return app;
    }

    /**
     * Gets the check suite this check run belongs to.
     *
     * @return Check suite
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
    public GHCheckSuite getCheckSuite() {
        return checkSuite;
    }

    /**
     * Gets an output for a check run.
     *
     * @return Output of a check run
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
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
        private String annotationsUrl;

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
            return GitHubClient.parseURL(annotationsUrl);
        }
    }

    /**
     * The Enum AnnotationLevel.
     */
    public static enum AnnotationLevel {
        
        /** The notice. */
        NOTICE, 
 /** The warning. */
 WARNING, 
 /** The failure. */
 FAILURE
    }

    /**
     * Updates this check run.
     *
     * @return a builder which you should customize, then call {@link GHCheckRunBuilder#create}
     */
    @Preview(Previews.ANTIOPE)
    public @NonNull GHCheckRunBuilder update() {
        return new GHCheckRunBuilder(owner, getId());
    }

}
