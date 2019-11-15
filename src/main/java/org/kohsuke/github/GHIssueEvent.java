package org.kohsuke.github;

import java.util.Date;

/**
 * The type GHIssueEvent.
 *
 * @author Martin van Zijl
 */
public class GHIssueEvent {
    private GitHub root;

    private long id;
    private String node_id;
    private String url;
    private GHUser actor;
    private String event;
    private String commit_id;
    private String commit_url;
    private String created_at;

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
        return GitHub.parseDate(created_at);
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
