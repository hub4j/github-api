package org.kohsuke.github;

/**
 * Represents the one page of check-runs result when listing check-runs.
 */
class GHCheckRunsPage {
    private int total_count;
    private GHCheckRun[] check_runs;

    public int getTotalCount() {
        return total_count;
    }

    GHCheckRun[] getCheckRuns(GitHub root) {
        for (GHCheckRun check_run : check_runs) {
            check_run.wrap(root);
        }
        return check_runs;
    }
}
