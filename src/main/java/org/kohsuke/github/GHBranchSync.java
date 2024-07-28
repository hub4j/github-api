package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class GHBranchSync extends GitHubInteractiveObject {

    private GHRepository owner;

    private String message;

    private String mergeType;

    private String baseBranch;

    /**
     * Gets owner.
     *
     * @return the repository that this branch is in.
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHRepository getOwner() {
        return owner;
    }

    public String getMessage() {
        return message;
    }

    public String getMergeType() {
        return mergeType;
    }

    public String getBaseBranch() {
        return baseBranch;
    }

    @Override
    public String toString() {
        return "GHBranchSync{" + "message='" + message + '\'' + ", mergeType='" + mergeType + '\'' + ", baseBranch='"
                + baseBranch + '\'' + '}';
    }

    /**
     * Wrap.
     *
     * @param repo
     *            the repo
     * @return the GH branch sync
     */
    GHBranchSync wrap(GHRepository repo) {
        this.owner = repo;
        return this;
    }

}
