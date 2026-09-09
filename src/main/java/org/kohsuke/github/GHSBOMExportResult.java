package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Represents the result of exporting an SBOM from a repository.
 *
 * @see GHRepository#getSBOM()
 * @see <a href="https://docs.github.com/en/rest/dependency-graph/sboms">GitHub SBOM API</a>
 */
@SuppressFBWarnings(value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD" },
        justification = "JSON API")
public class GHSBOMExportResult {

    private GHSBOM sbom;

    /**
     * Create default GHSBOMExportResult instance.
     */
    public GHSBOMExportResult() {
    }

    /**
     * Gets the SBOM.
     *
     * @return the SBOM
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHSBOM getSbom() {
        return sbom;
    }
}
