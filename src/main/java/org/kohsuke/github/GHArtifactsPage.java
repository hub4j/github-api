package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

// TODO: Auto-generated Javadoc
/**
 * Represents the one page of artifacts result when listing artifacts.
 */
@SuppressFBWarnings(value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD" },
        justification = "JSON API")
class GHArtifactsPage {
    private int total_count;
    private GHArtifact[] artifacts;

    /**
     * Gets the total count.
     *
     * @return the total count
     */
    public int getTotalCount() {
        return total_count;
    }

    /**
     * Gets the artifacts.
     *
     * @param owner
     *            the owner
     * @return the artifacts
     */
    GHArtifact[] getArtifacts(GHRepository owner) {
        for (GHArtifact artifact : artifacts) {
            artifact.wrapUp(owner);
        }
        return artifacts;
    }
}
