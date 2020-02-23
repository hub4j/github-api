package org.kohsuke.github;

import java.io.IOException;

/**
 * Those {@link GHObject}s that can have {@linkplain GHReaction reactions}.
 */
@Preview
@Deprecated
public interface Reactable {
    /**
     * List all the reactions left to this object.
     *
     * @return the paged iterable
     */
    @Preview
    @Deprecated
    PagedIterable<GHReaction> listReactions();

    /**
     * Leaves a reaction to this object.
     *
     * @param content
     *            the content
     * @return the gh reaction
     * @throws IOException
     *             the io exception
     */
    @Preview
    @Deprecated
    GHReaction createReaction(ReactionContent content) throws IOException;
}
