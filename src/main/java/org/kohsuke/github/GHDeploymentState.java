package org.kohsuke.github;

// TODO: Auto-generated Javadoc
/**
 * Represents the state of deployment.
 */
public enum GHDeploymentState {

    /** The error. */
    ERROR,

    /** The failure. */
    FAILURE,

    /**
     * The state of the deployment currently reflects it's no longer active.
     */
    INACTIVE,

    /**
     * The state of the deployment currently reflects it's in progress.
     */
    IN_PROGRESS,

    /** The pending. */
    PENDING,

    /**
     * The state of the deployment currently reflects it's queued up for processing.
     */
    QUEUED,

    /** The success. */
    SUCCESS
}
