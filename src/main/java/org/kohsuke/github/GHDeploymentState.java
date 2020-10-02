package org.kohsuke.github;

/**
 * Represents the state of deployment
 */
public enum GHDeploymentState {
    PENDING, SUCCESS, ERROR, FAILURE,

    @Preview(Previews.FLASH)
    IN_PROGRESS,

    @Preview(Previews.FLASH)
    QUEUED,

    @Preview(Previews.ANT_MAN)
    INACTIVE
}
