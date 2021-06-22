package org.kohsuke.github;

/**
 * Represents the one page of GHAppInstallations.
 */
class GHAppInstallationsPage {
    private int total_count;
    private GHAppInstallation[] installations;

    public int getTotalCount() {
        return total_count;
    }

    GHAppInstallation[] getInstallations() {
        return installations;
    }
}
