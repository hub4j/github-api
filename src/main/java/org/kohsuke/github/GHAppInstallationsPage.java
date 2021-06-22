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

    GHAppInstallation[] getInstallations(GitHub root) {
        for (GHAppInstallation installation : installations) {
            installation.wrap(root);
        }
        return installations;
    }
}
