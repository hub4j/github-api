package org.kohsuke.github;

import java.util.Date;

/**
 * The type GHIssueEvent.
 *
 * @see <a href="https://developer.github.com/v3/issues/events/">Github documentation for issue events</a>
 *
 * @author Martin van Zijl
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
     * Gets root.
     *
     * @return the root
     */
    public GitHub getRoot() {
        return root;
    }

    /**
     * Gets issue.
     *
     * @return the issue
     */
    public GHIssue getIssue() {
        return issue;
    }

    /**
     * Get the {@link GHMilestone} that this issue was added to or removed from. Only present for events "milestoned"
     * and "demilestoned", <code>null</code> otherwise.
     *
     * @return the milestone
     */
    public GHMilestone getMilestone() {
        return milestone;
    }

    /**
     * Get the {@link GHLabel} that was added to or removed from the issue. Only present for events "labeled" and
     * "unlabeled", <code>null</code> otherwise.
     *
     * @return the label
     */
    public GHLabel getLabel() {
        return label;
    }

    /**
     * Get the {@link GHUser} that was assigned or unassigned from the issue. Only present for events "assigned" and
     * "unassigned", <code>null</code> otherwise.
     *
     * @return the user
     */
    public GHUser getAssignee() {
        return assignee;
    }

    GHIssueEvent wrapUp(GitHub root) {
        this.root = root;
        return this;
    }

    GHIssueEvent wrapUp(GHIssue parent) {
        this.issue = parent;
        this.root = parent.root;
        return this;
    }

    @Override
    public String toString() {
        return String.format("Issue %d was %s by %s on %s",
                getIssue().getNumber(),
                getEvent(),
                getActor().getLogin(),
                getCreatedAt().toString());
    }
}
