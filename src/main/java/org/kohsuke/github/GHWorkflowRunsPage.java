package org.kohsuke.github;

// TODO: Auto-generated Javadoc
/**
 * Represents the one page of workflow runs result when listing workflow runs.
 */
class GHWorkflowRunsPage {
    private int totalCount;
    private GHWorkflowRun[] workflowRuns;

    /**
     * Gets the total count.
     *
     * @return the total count
     */
    public int getTotalCount() {
        return totalCount;
    }

    /**
     * Gets the workflow runs.
     *
     * @param owner the owner
     * @return the workflow runs
     */
    GHWorkflowRun[] getWorkflowRuns(GHRepository owner) {
        for (GHWorkflowRun workflowRun : workflowRuns) {
            workflowRun.wrapUp(owner);
        }
        return workflowRuns;
    }
}
