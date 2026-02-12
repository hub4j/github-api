package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import community.kotlin.conrib.github.Reactable;
import community.kotlin.conrib.github.ReactionContent;

// TODO: Auto-generated Javadoc
/**
 * Reaction to issue, comment, PR, and so on.
 *
 * @author Kohsuke Kawaguchi
 * @see Reactable
 */
public class GHReaction extends GHObject
        implements community.kotlin.conrib.github.GHReaction {

    private ReactionContent content;

    private GHUser user;
    /**
     * Create default GHReaction instance
     */
    public GHReaction() {
    }

    /**
     * The kind of reaction left.
     *
     * @return the content
     */
    public ReactionContent getContent() {
        return content;
    }

    /**
     * User who left the reaction.
     *
     * @return the user
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHUser getUser() {
        return user;
    }
}
