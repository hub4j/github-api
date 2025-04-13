package org.kohsuke.github;

import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.time.Instant;
import java.util.Date;

// TODO: Auto-generated Javadoc
/**
 * The type GHIssueEvent.
 *
 * @author Martin van Zijl
 * @see <a href="https://developer.github.com/v3/issues/events/">Github documentation for issue events</a>
 */
public class GHIssueEvent extends GitHubInteractiveObject {

    private GHUser actor;

    private GHUser assignee;
    private String commitId;
    private String commitUrl;
    private String createdAt;
    private String event;
    private long id;
    private GHIssue issue;
    private GHLabel label;
    private GHMilestone milestone;
    private String nodeId;
    private GHIssueRename rename;
    private GHUser requestedReviewer;
    private GHUser reviewRequester;
    private String url;

    /**
     * Create default GHIssueEvent instance
     */
    public GHIssueEvent() {
    }

    /**
     * Gets actor.
     *
     * @return the actor
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHUser getActor() {
        return actor;
    }

    /**
     * Get the {@link GHUser} that was assigned or unassigned from the issue. Only present for events "assigned" and
     * "unassigned", <code>null</code> otherwise.
     *
     * @return the user
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHUser getAssignee() {
        return assignee;
    }

    /**
     * Gets commit id.
     *
     * @return the commit id
     */
    public String getCommitId() {
        return commitId;
    }

    /**
     * Gets commit url.
     *
     * @return the commit url
     */
    public String getCommitUrl() {
        return commitUrl;
    }

    /**
     * Gets created at.
     *
     * @return the created at
     */
    @WithBridgeMethods(value = Date.class, adapterMethod = "instantToDate")
    public Instant getCreatedAt() {
        return GitHubClient.parseInstant(createdAt);
    }

    /**
     * Gets event.
     *
     * @return the event
     */
    public String getEvent() {
        return event;
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * Gets issue.
     *
     * @return the issue
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHIssue getIssue() {
        return issue;
    }

    /**
     * Get the {@link GHLabel} that was added to or removed from the issue. Only present for events "labeled" and
     * "unlabeled", <code>null</code> otherwise.
     *
     * @return the label
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHLabel getLabel() {
        return label;
    }

    /**
     * Get the {@link GHMilestone} that this issue was added to or removed from. Only present for events "milestoned"
     * and "demilestoned", <code>null</code> otherwise.
     *
     * @return the milestone
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHMilestone getMilestone() {
        return milestone;
    }

    /**
     * Gets node id.
     *
     * @return the node id
     */
    public String getNodeId() {
        return nodeId;
    }

    /**
     * Get the {@link GHIssueRename} that contains information about issue old and new name. Only present for event
     * "renamed", <code>null</code> otherwise.
     *
     * @return the GHIssueRename
     */
    public GHIssueRename getRename() {
        return this.rename;
    }

    /**
     *
     * Get the {@link GHUser} person requested to review the pull request. Only present for events "review_requested",
     * "review_request_removed", <code>null</code> otherwise.
     *
     * @return the GHUser
     *
     * @see <a href=
     *      "https://docs.github.com/en/developers/webhooks-and-events/events/issue-event-types#review_requested">review_requested</a>
     *      and <a href=
     *      "https://docs.github.com/en/developers/webhooks-and-events/events/issue-event-types#review_request_removed">review_request_removed</a>
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHUser getRequestedReviewer() {
        return this.requestedReviewer;
    }

    /**
     *
     * Get the {@link GHUser} person who requested a review. Only present for events "review_requested",
     * "review_request_removed", <code>null</code> otherwise.
     *
     * @return the GHUser
     *
     * @see <a href=
     *      "https://docs.github.com/en/developers/webhooks-and-events/events/issue-event-types#review_requested">review_requested</a>
     *      and <a href=
     *      "https://docs.github.com/en/developers/webhooks-and-events/events/issue-event-types#review_request_removed">review_request_removed</a>
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHUser getReviewRequester() {
        return this.reviewRequester;
    }

    /**
     * Gets url.
     *
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return String.format("Issue %d was %s by %s on %s",
                getIssue().getNumber(),
                getEvent(),
                getActor().getLogin(),
                getCreatedAt().toString());
    }

    /**
     * Wrap up.
     *
     * @param parent
     *            the parent
     * @return the GH issue event
     */
    GHIssueEvent wrapUp(GHIssue parent) {
        this.issue = parent;
        return this;
    }
}
