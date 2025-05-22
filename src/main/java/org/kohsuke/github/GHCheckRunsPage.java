package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

// TODO: Auto-generated Javadoc
/**
 * Represents the one page of check-runs result when listing check-runs.
 */
@SuppressFBWarnings(value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD" },
        justification = "JSON API")
class GHCheckRunsPage {
    private int total_count;
    private GHCheckRun[] check_runs;

    /**
     * Gets the total count.
     *
     * @return the total count
     */
    public int getTotalCount() {
        return total_count;
    }

    /**
     * Gets the check runs.
     *
     * @param owner
     *            the owner
     * @return the check runs
     */
    GHCheckRun[] getCheckRuns(GHRepository owner) {
        for (GHCheckRun check_run : check_runs) {
            check_run.wrap(owner);
        }
        return check_runs;
    }
}
