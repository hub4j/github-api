package org.kohsuke.github;

/**
 * Represents the one page of jobs result when listing jobs from a workflow run.
 */
class GHWorkflowRunJobsPage {
    private int total_count;
    private GHWorkflowRunJob[] jobs;

    public int getTotalCount() {
        return total_count;
    }

    GHWorkflowRunJob[] getWorkflowRunJobs(GHRepository repo) {
        for (GHWorkflowRunJob job : jobs) {
            job.wrapUp(repo);
        }
        return jobs;
    }
}
