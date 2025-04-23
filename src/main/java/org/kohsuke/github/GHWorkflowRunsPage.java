package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

// TODO: Auto-generated Javadoc
/**
 * Represents the one page of workflow runs result when listing workflow runs.
 */
@SuppressFBWarnings(value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD" },
        justification = "JSON API")
class GHWorkflowRunsPage implements GitHubPage<GHWorkflowRun> {
    private int totalCount;
    private GHWorkflowRun[] workflowRuns;

    @Override
    public GHWorkflowRun[] getItems() {
        return workflowRuns;
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
