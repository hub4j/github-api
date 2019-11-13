package org.kohsuke.github;

import java.io.IOException;

/**
 * Those {@link GHObject}s that can have {@linkplain GHReaction reactions}.
 *
 * @author Kohsuke Kawaguchi
 */
@Preview
@Deprecated
public interface Reactable {
    /**
     * List all the reactions left to this object.
     */
    @Preview
    @Deprecated
    PagedIterable<GHReaction> listReactions();

    /**
     * Leaves a reaction to this object.
     */
    @Preview
    @Deprecated
    GHReaction createReaction(ReactionContent content) throws IOException;
}
