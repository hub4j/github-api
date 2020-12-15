package org.kohsuke.github;

import java.io.IOException;

import static org.kohsuke.github.Previews.SQUIRREL_GIRL;

/**
 * Those {@link GHObject}s that can have {@linkplain GHReaction reactions}.
 *
 * @author Kohsuke Kawaguchi
 */
@Preview(SQUIRREL_GIRL)
@Deprecated
public interface Reactable {
    /**
     * List all the reactions left to this object.
     *
     * @return the paged iterable
     */
    @Preview(SQUIRREL_GIRL)
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
    @Preview(SQUIRREL_GIRL)
    @Deprecated
    GHReaction createReaction(ReactionContent content) throws IOException;
}
