package org.kohsuke.github;

// TODO: Auto-generated Javadoc
/**
 * Represents the one page of GHAppInstallations.
 */
class GHAppInstallationsPage {
    private int totalCount;
    private GHAppInstallation[] installations;

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
