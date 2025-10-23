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

    private GHCommit commit;

    private String name;

    private GHRepository owner;
    /**
     * Create default GHTag instance
     */
    public GHTag() {
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

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
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
     * Wrap.
     *
     * @param owner
     *            the owner
     * @return the GH tag
     */
    GHTag wrap(GHRepository owner) {
        this.owner = owner;
        if (commit != null)
            commit.wrapUp(owner);
        return this;
    }
}
