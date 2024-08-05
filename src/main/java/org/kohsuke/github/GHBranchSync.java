package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * The type Gh branch sync.
 */
public class GHBranchSync extends GitHubInteractiveObject {

    /**
     * The Repository that this branch is in.
     */
    private GHRepository owner;

    /**
     * The message.
     */
    private String message;

    /**
     * The merge type.
     */
    private String mergeType;

    /**
     * The base branch.
     */
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

    /**
     * Gets message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Gets merge type.
     *
     * @return the merge type
     */
    public String getMergeType() {
        return mergeType;
    }

    /**
     * Gets base branch.
     *
     * @return the base branch
     */
    public String getBaseBranch() {
        return baseBranch;
    }

    /**
     * To string.
     *
     * @return the string
     */
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
