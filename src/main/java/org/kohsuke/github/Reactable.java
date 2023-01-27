package org.kohsuke.github;

import java.io.IOException;

import static org.kohsuke.github.internal.Previews.SQUIRREL_GIRL;

// TODO: Auto-generated Javadoc
/**
 * Those {@link GHObject}s that can have {@linkplain GHReaction reactions}.
 *
 * @author Kohsuke Kawaguchi
 */
@Preview(SQUIRREL_GIRL)
public interface Reactable {
    /**
     * List all the reactions left to this object.
     *
     * @return the paged iterable
     */
    @Preview(SQUIRREL_GIRL)
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
    @Preview(SQUIRREL_GIRL)
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
