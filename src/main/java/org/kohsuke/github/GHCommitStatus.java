package org.kohsuke.github;

import java.io.IOException;
import java.net.URL;

// TODO: Auto-generated Javadoc
/**
 * Represents a status of a commit.
 *
 * @author Kohsuke Kawaguchi
 * @see GHRepository#getLastCommitStatus(String) GHRepository#getLastCommitStatus(String)
 * @see GHCommit#getLastStatus() GHCommit#getLastStatus()
 * @see GHRepository#createCommitStatus(String, GHCommitState, String, String) GHRepository#createCommitStatus(String,
 *      GHCommitState, String, String)
 */
public class GHCommitStatus extends GHObject {

    /** The state. */
    String state;

    /** The description. */
    String target_url, description;

    /** The context. */
    String context;

    /** The creator. */
    GHUser creator;

    /**
     * Gets state.
     *
     * @return the state
     */
    public GHCommitState getState() {
        for (GHCommitState s : GHCommitState.values()) {
            if (s.name().equalsIgnoreCase(state))
                return s;
        }
        throw new IllegalStateException("Unexpected state: " + state);
    }

    /**
     * The URL that this status is linked to.
     * <p>
     * This is the URL specified when creating a commit status.
     *
     * @return the target url
     */
    public String getTargetUrl() {
        return target_url;
    }

    /**
     * Gets description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets creator.
     *
     * @return the creator
     * @throws IOException
     *             the io exception
     */
    public GHUser getCreator() throws IOException {
        return root().intern(creator);
    }

    /**
     * Gets context.
     *
     * @return the context
     */
    public String getContext() {
        return context;
    }

    /**
     * Gets the html url.
     *
     * @return the html url
     * @deprecated This object has no HTML URL.
     */
    @Override
    public URL getHtmlUrl() {
        return null;
    }
}
