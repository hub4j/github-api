package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.net.URL;

import static org.kohsuke.github.internal.Previews.SQUIRREL_GIRL;

/**
 * Reaction to issue, comment, PR, and so on.
 *
 * @author Kohsuke Kawaguchi
 * @see Reactable
 */
@Preview(SQUIRREL_GIRL)
public class GHReaction extends GHObject {

    private GHUser user;
    private ReactionContent content;

    GHReaction wrap(GitHub root) {
        this.root = root;
        user.wrapUp(root);
        return this;
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

    /**
     * Reaction has no HTML URL. Don't call this method.
     */
    @Deprecated
    public URL getHtmlUrl() {
        return null;
    }

    /**
     * Removes this reaction.
     *
     * @throws IOException
     *             the io exception
     */
    public void delete() throws IOException {
        root.createRequest().method("DELETE").withPreview(SQUIRREL_GIRL).withUrlPath("/reactions/" + getId()).send();
    }
}
