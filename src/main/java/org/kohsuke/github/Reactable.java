package org.kohsuke.github;

import java.io.IOException;

// TODO: Auto-generated Javadoc
/**
 * Those {@link GHObject}s that can have {@linkplain GHReaction reactions}.
 *
 * @author Kohsuke Kawaguchi
 */
public interface Reactable {
    /**
     * List all the reactions left to this object.
     *
     * @return the paged iterable
     */
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
    GHReaction createReaction(ReactionContent content) throws IOException;

    /**
     * Delete a reaction from this object.
     *
     * @param reaction
     *            the reaction to delete
     * @throws IOException
     *             the io exception
     */
    void deleteReaction(GHReaction reaction) throws IOException;
}
