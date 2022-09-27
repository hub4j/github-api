package org.kohsuke.github;

// TODO: Auto-generated Javadoc
/**
 * Represents the one page of workflow result when listing workflows.
 */
class GHWorkflowsPage {
    private int total_count;
    private GHWorkflow[] workflows;

    /**
     * Gets the total count.
     *
     * @return the total count
     */
    public int getTotalCount() {
        return total_count;
    }

    /**
     * Gets the workflows.
     *
     * @param owner the owner
     * @return the workflows
     */
    GHWorkflow[] getWorkflows(GHRepository owner) {
        for (GHWorkflow workflow : workflows) {
            workflow.wrapUp(owner);
        }
        return workflows;
    }
}
