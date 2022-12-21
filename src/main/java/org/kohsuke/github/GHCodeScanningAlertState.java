package org.kohsuke.github;

/**
 * What is the current state of the Alert
 */
public enum GHCodeScanningAlertState {
    /**
     * Alert is open and still an active issue.
     */
    OPEN,
    /**
     * Issue that has caused the alert has been addressed.
     */
    FIXED,
    /**
     * Alert has been dismissed by a user without being fixed.
     */
    DISMISSED
}
