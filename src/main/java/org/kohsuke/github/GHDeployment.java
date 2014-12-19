package org.kohsuke.github;

public class GHDeployment {
    private GHRepository owner;
    private GitHub root;

    GHDeployment wrap(GHRepository owner) {
        this.owner = owner;
        this.root = owner.root;
        return this;
    }
}
