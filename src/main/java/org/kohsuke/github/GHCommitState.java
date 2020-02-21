package org.kohsuke.github;

/**
 * Represents the state of commit
 *
 * @see GHCommitStatus
 */
public enum GHCommitState {
    PENDING, SUCCESS, ERROR, FAILURE
}
