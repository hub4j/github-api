package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

// TODO: Auto-generated Javadoc
/**
 * Represents a tag in {@link GHRepository}.
 *
 * @see GHRepository#listTags() GHRepository#listTags()
 */
@SuppressFBWarnings(value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD" },
        justification = "JSON API")
public class GHTag extends GitHubInteractiveObject {
    private GHRepository owner;

    private String name;
    private GHCommit commit;

    /**
     * Wrap.
     *
     * @param owner the owner
     * @return the GH tag
     */
    GHTag wrap(GHRepository owner) {
        this.owner = owner;
        if (commit != null)
            commit.wrapUp(owner);
        return this;
    }

    /**
     * Gets owner.
     *
     * @return the owner
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHRepository getOwner() {
        return owner;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets commit.
     *
     * @return the commit
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHCommit getCommit() {
        return commit;
    }
}
