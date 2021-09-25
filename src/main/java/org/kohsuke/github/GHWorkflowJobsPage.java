package org.kohsuke.github;

/**
 * Represents the one page of jobs result when listing jobs from a workflow run.
 */
class GHWorkflowJobsPage {
    private int total_count;
    private GHWorkflowJob[] jobs;

    public int getTotalCount() {
        return total_count;
    }

    GHWorkflowJob[] getWorkflowJobs(GHRepository repo) {
        for (GHWorkflowJob job : jobs) {
            job.wrapUp(repo);
        }
        return jobs;
    }
}
