package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.net.URL;

import static org.kohsuke.github.internal.Previews.SQUIRREL_GIRL;

// TODO: Auto-generated Javadoc
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
     *
     * @return the html url
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
     * @see <a href="https://github.blog/changelog/2022-02-11-legacy-delete-reactions-rest-api-removed/">Legacy Delete
     *      reactions REST API removed</a>
     * @deprecated this API is no longer supported by GitHub, keeping it as is for old versions of GitHub Enterprise
     */
    @Deprecated
    public void delete() throws IOException {
        throw new UnsupportedOperationException(
                "This method is not supported anymore. Please use Reactable#deleteReaction(GHReaction).");
    }
}
