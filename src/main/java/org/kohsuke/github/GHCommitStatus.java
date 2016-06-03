package org.kohsuke.github;

import java.net.URL;

/**
 * Represents a status of a commit.
 *
 * @author Kohsuke Kawaguchi
 * @see GHRepository#getLastCommitStatus(String)
 * @see GHCommit#getLastStatus()
 * @see GHRepository#createCommitStatus(String, GHCommitState, String, String)
 */
public class GHCommitStatus extends GHObject {
    String state;
    String target_url,description;
    String context;
    GHUser creator;

    private GitHub root;

    /*package*/ GHCommitStatus wrapUp(GitHub root) {
        if (creator!=null)  creator.wrapUp(root);
        this.root = root;
        return this;
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

    public GHUser getCreator() {
        return creator;
    }

    public String getContext() {
        return context;
    }

    /**
     * @deprecated This object has no HTML URL.
     */
    @Override
    public URL getHtmlUrl() {
        return null;
    }
}
