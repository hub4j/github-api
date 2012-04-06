package org.kohsuke.github;

/**
 * A branch in a repository.
 * 
 * @author Yusuke Kokubo
 */
public class GHBranch {
    private GitHub root;
    private GHRepository owner;

    private String name;
    private Commit commit;

    public static class Commit {
        String sha,url;
    }

    public GitHub getRoot() {
        return root;
    }

    /**
     * Repository that this branch is in.
     */
    public GHRepository getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    /**
     * The commit that this branch currently points to.
     */
    public String getSHA1() {
        return commit.sha;
    }

    @Override
    public String toString() {
        return "Branch:" + name + " in " + owner.getUrl();
    }

    /*package*/ GHBranch wrap(GHRepository repo) {
        this.owner = repo;
        this.root = repo.root;
        return this;
    }
}
