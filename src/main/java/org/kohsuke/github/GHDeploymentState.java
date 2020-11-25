package org.kohsuke.github;

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
     *
     * @deprecated until preview feature has graduated to stable
     */
    @Deprecated
    @Preview(Previews.FLASH)
    IN_PROGRESS,

    /**
     * The state of the deployment currently reflects it's queued up for processing.
     *
     * @deprecated until preview feature has graduated to stable
     */
    @Deprecated
    @Preview(Previews.FLASH)
    QUEUED,

    /**
     * The state of the deployment currently reflects it's no longer active.
     *
     * @deprecated until preview feature has graduated to stable
     */
    @Deprecated
    @Preview(Previews.ANT_MAN)
    INACTIVE
}
