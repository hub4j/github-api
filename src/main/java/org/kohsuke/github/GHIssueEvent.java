package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Date;

// TODO: Auto-generated Javadoc
/**
 * The type GHIssueEvent.
 *
 * @author Martin van Zijl
 * @see <a href="https://developer.github.com/v3/issues/events/">Github documentation for issue events</a>
 */
public class GHIssueEvent extends GitHubInteractiveObject {
    private long id;
    private String node_id;
    private String url;
    private GHUser actor;
    private String event;
    private String commit_id;
    private String commit_url;
    private String created_at;
    private GHMilestone milestone;
    private GHLabel label;
    private GHUser assignee;
    private GHIssueRename rename;
    private GHUser reviewRequester;
    private GHUser requestedReviewer;

    private GHIssue issue;

    /**
     * Gets id.
     *
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * Gets node id.
     *
     * @return the node id
     */
    public String getNodeId() {
        return node_id;
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
     * Gets actor.
     *
     * @return the actor
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHUser getActor() {
        return actor;
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
     * Gets commit id.
     *
     * @return the commit id
     */
    public String getCommitId() {
        return commit_id;
    }

    /**
     * Gets commit url.
     *
     * @return the commit url
     */
    public String getCommitUrl() {
        return commit_url;
    }

    /**
     * Gets created at.
     *
     * @return the created at
     */
    public Date getCreatedAt() {
        return GitHubClient.parseDate(created_at);
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
     * Wrap up.
     *
     * @param parent the parent
     * @return the GH issue event
     */
    GHIssueEvent wrapUp(GHIssue parent) {
        this.issue = parent;
        return this;
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
}
