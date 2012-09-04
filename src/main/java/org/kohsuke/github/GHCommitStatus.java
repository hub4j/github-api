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

    /**
     * Updates the description.
     *
     * TODO: verify if this actually works, and create setTargetUrl, too.
     */
    public void setDescription(String description) throws IOException {
        new Poster(root)
                .with("description",description)
                .withCredential()
                .to(url,null,"PATCH");
        this.description = description;
    }

    // TODO: verify if it works
    public void delete() throws IOException {
        new Poster(root).withCredential().to(url,null,"DELETE");
    }
}
