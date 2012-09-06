package org.kohsuke.github;

import java.io.IOException;
import java.util.Date;

/**
 * Represents a status of a commit.
 *
 * @author Kohsuke Kawaguchi
 * @see GHRepository#getCommitStatus(String)
 * @see GHCommit#getStatus()
 */
public class GHCommitStatus {
    String created_at, updated_at;
    String state;
    String target_url,description;
    int id;
    String url;
    GHUser creator;

    private GitHub root;

    /*package*/ GHCommitStatus wrapUp(GitHub root) {
        if (creator!=null)  creator.wrapUp(root);
        this.root = root;
        return this;
    }

    public Date getCreatedAt() {
        return GitHub.parseDate(created_at);
    }

    public Date getUpdatedAt() {
        return GitHub.parseDate(updated_at);
    }

    public GHCommitState getState() {
        for (GHCommitState s : GHCommitState.values()) {
            if (s.name().equalsIgnoreCase(state))
                return s;
        }
        throw new IllegalStateException("Unexpected state: "+state);
    }

    /**
     * The URL that this status is linked to.
     *
     * This is the URL specified when creating a commit status.
     */
    public String getTargetUrl() {
        return target_url;
    }

    public String getDescription() {
        return description;
    }

    public int getId() {
        return id;
    }

    /**
     * API URL of this commit status.
     */
    public String getUrl() {
        return url;
    }

    public GHUser getCreator() {
        return creator;
    }
}
