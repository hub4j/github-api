package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

// TODO: Auto-generated Javadoc
/**
 * Represents the one page of check-runs result when listing check-runs.
 */
@SuppressFBWarnings(value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD" },
        justification = "JSON API")
class GHCheckRunsPage implements GitHubPage<GHCheckRun> {
    private GHCheckRun[] checkRuns;
    private int totalCount;

    @Override
    public GHCheckRun[] getItems() {
        return checkRuns;
    }

    /**
     * Gets the total count.
     *
     * @return the total count
     */
    public int getTotalCount() {
        return totalCount;
    }
}
