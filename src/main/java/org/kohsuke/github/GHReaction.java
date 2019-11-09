package org.kohsuke.github;

import java.io.IOException;
import java.net.URL;

import static org.kohsuke.github.Previews.*;

/**
 * Reaction to issue, comment, PR, and so on.
 *
 * @author Kohsuke Kawaguchi
 * @see Reactable
 */
@Preview @Deprecated
public class GHReaction extends GHObject {
    private GHUser user;
    private ReactionContent content;

    /*package*/ GHReaction wrap(GitHub root) {
        user.wrapUp(root);
        return this;
    }

    /**
     * The kind of reaction left.
     */
    public ReactionContent getContent() {
        return content;
    }

    /**
     * User who left the reaction.
     */
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
     */
    public void delete() throws IOException {
        new Requester(getRoot()).method("DELETE").withPreview(SQUIRREL_GIRL).to("/reactions/"+id);
    }
}
