package org.kohsuke.github;

/**
 * Represents the one page of workflow result when listing workflows.
 */
class GHWorkflowsPage {
    private int total_count;
    private GHWorkflow[] workflows;

    public int getTotalCount() {
        return total_count;
    }

    GHWorkflow[] getWorkflows(GHRepository owner) {
        for (GHWorkflow workflow : workflows) {
            workflow.wrapUp(owner);
        }
        return workflows;
    }
}
