package org.kohsuke.github;

/**
 * Represents a tag in {@link GHRepository}
 *
 * @see GHRepository#listTags()
 */
public class GHTag {
    private GHRepository owner;
    private GitHub root;

    private String name;
    private GHCommit commit;

    /*package*/ GHTag wrap(GHRepository owner) {
        this.owner = owner;
        this.root = owner.root;
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
