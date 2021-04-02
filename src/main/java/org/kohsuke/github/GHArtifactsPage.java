package org.kohsuke.github;

/**
 * Represents the one page of artifacts result when listing artifacts.
 */
class GHArtifactsPage {
    private int total_count;
    private GHArtifact[] artifacts;

    public int getTotalCount() {
        return total_count;
    }

    GHArtifact[] getArtifacts(GHRepository owner) {
        for (GHArtifact artifact : artifacts) {
            artifact.wrapUp(owner);
        }
        return artifacts;
    }
}
