package org.kohsuke.github;

import org.kohsuke.github.internal.Previews;

/**
 * Represents the state of deployment
 */
public enum GHDeploymentState {
    PENDING,
    SUCCESS,
    ERROR,
    FAILURE,

    /**
     * The state of the deployment currently reflects it's in progress.
     */
    @Preview(Previews.FLASH)
    IN_PROGRESS,

    /**
     * The state of the deployment currently reflects it's queued up for processing.
     */
    @Preview(Previews.FLASH)
    QUEUED,

    /**
     * The state of the deployment currently reflects it's no longer active.
     */
    @Preview(Previews.ANT_MAN)
    INACTIVE
}
