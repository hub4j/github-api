package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

// TODO: Auto-generated Javadoc
/**
 * Reaction to issue, comment, PR, and so on.
 *
 * @author Kohsuke Kawaguchi
 * @see Reactable
 */
public class GHReaction extends GHObject {

    private GHUser user;
    private ReactionContent content;

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
