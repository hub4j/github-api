package org.kohsuke.github;


public class GHTag {
    private GHRepository owner;
    private GitHub root;

    private String name;
    private GHCommit commit;

    GHTag wrap(GHRepository owner) {
        this.owner = owner;
        this.root = owner.root;
        return this;
    }

    static GHTag[] wrap(GHTag[] tags, GHRepository owner) {
        for (GHTag tag : tags) {
            tag.wrap(owner);
        }
        return tags;
    }


    public GHRepository getOwner() {
        return owner;
    }

    public void setOwner(GHRepository owner) {
        this.owner = owner;
    }

    public GitHub getRoot() {
        return root;
    }

    public void setRoot(GitHub root) {
        this.root = root;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GHCommit getCommit() {
        return commit;
    }

    public void setCommit(GHCommit commit) {
        this.commit = commit;
    }
}
