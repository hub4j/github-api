package org.kohsuke.github;

/**
 * Represents the one page of workflow runs result when listing workflow runs.
 */
class GHWorkflowRunsPage {
    private int totalCount;
    private GHWorkflowRun[] workflowRuns;

    public int getTotalCount() {
        return totalCount;
    }

    GHWorkflowRun[] getWorkflowRuns(GHRepository owner) {
        for (GHWorkflowRun workflowRun : workflowRuns) {
            workflowRun.wrapUp(owner);
        }
        return workflowRuns;
    }
}
