package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Represents a tag in {@link GHRepository}
 *
 * @see GHRepository#listTags()
 */
@SuppressFBWarnings(value = {"UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", 
    "NP_UNWRITTEN_FIELD"}, justification = "JSON API")
public class GHTag {
    private GHRepository owner;
    private GitHub root;

    private String name;
    private GHCommit commit;

    /*package*/ GHTag wrap(GHRepository owner) {
        this.owner = owner;
        this.root = owner.root;
        if (commit!=null)
            commit.wrapUp(owner);
        return this;
    }

    public GHRepository getOwner() {
        return owner;
    }

    public GitHub getRoot() {
        return root;
    }

    public String getName() {
        return name;
    }

    public GHCommit getCommit() {
        return commit;
    }
}
