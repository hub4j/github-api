package org.kohsuke.github;

// TODO: Auto-generated Javadoc
/**
 * Represents the one page of GHAppInstallations.
 */
class GHAppInstallationsPage {
    private GHAppInstallation[] installations;
    private int totalCount;

    /**
     * Gets the total count.
     *
     * @return the total count
     */
    public int getTotalCount() {
        return totalCount;
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
