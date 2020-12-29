package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Represents a tag in {@link GHRepository}
 *
 * @see GHRepository#listTags() GHRepository#listTags()
 */
@SuppressFBWarnings(value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD" },
        justification = "JSON API")
public class GHTag extends GitHubInteractiveObject {
    private GHRepository owner;

    private String name;
    private GHCommit commit;

    GHTag wrap(GHRepository owner) {
        this.owner = owner;
        this.root = owner.root;
        if (commit != null)
            commit.wrapUp(owner);
        return this;
    }

    /**
     * Gets owner.
     *
     * @return the owner
     */
    public GHRepository getOwner() {
        return owner;
    }

    /**
     * Gets root.
     *
     * @return the root
     */
    public GitHub getRoot() {
        return root;
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
    public GHCommit getCommit() {
        return commit;
    }
}
