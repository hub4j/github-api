package org.kohsuke.github;

// TODO: Auto-generated Javadoc
/**
 * How is an user associated with a repository?.
 *
 * @author Kohsuke Kawaguchi
 */
public enum GHCommentAuthorAssociation {
    /**
     * Author has been invited to collaborate on the repository.
     */
    COLLABORATOR,
    /**
     * Author has previously committed to the repository.
     */
    CONTRIBUTOR,
    /**
     * Author has not previously committed to GitHub.
     */
    FIRST_TIMER,
    /**
     * Author has not previously committed to the repository.
     */
    FIRST_TIME_CONTRIBUTOR,
    /**
     * Author is a placeholder for an unclaimed user.
     */
    MANNEQUIN,    /**
     * Author is a member of the organization that owns the repository.
     */
    MEMBER,
    /**
     * Author has no association with the repository.
     */
    NONE,
    /**
     * Author is the owner of the repository.
     */
    OWNER
}
