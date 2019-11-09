package org.kohsuke.github;

import java.util.Date;

/**
 * @author Martin van Zijl
 */
public class GHIssueEvent extends GHObjectBase {
    private long id;
    private String node_id;
    private String url;
    private GHUser actor;
    private String event;
    private String commit_id;
    private String commit_url;
    private String created_at;

    private GHIssue issue;

    public long getId() {
        return id;
    }

    public String getNodeId() {
        return node_id;
    }

    public String getUrl() {
        return url;
    }

    public GHUser getActor() {
        return actor;
    }

    public String getEvent() {
        return event;
    }

    public String getCommitId() {
        return commit_id;
    }

    public String getCommitUrl() {
        return commit_url;
    }

    public Date getCreatedAt() {
        return GitHub.parseDate(created_at);
    }

    public GitHub getRoot() {
        return root;
    }

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
