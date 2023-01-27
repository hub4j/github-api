package org.kohsuke.github;

// TODO: Auto-generated Javadoc
/**
 * Represents the one page of GHAppInstallations.
 */
class GHAppInstallationsPage {
    private int total_count;
    private GHAppInstallation[] installations;

    /**
     * Gets the total count.
     *
     * @return the total count
     */
    public int getTotalCount() {
        return total_count;
    }

    /**
     * Gets the installations.
     *
     * @return the installations
     */
    GHAppInstallation[] getInstallations() {
        return installations;
    }
}
