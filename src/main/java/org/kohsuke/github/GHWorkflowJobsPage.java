package org.kohsuke.github;

// TODO: Auto-generated Javadoc
/**
 * Represents the one page of jobs result when listing jobs from a workflow run.
 */
class GHWorkflowJobsPage {
    private int total_count;
    private GHWorkflowJob[] jobs;

    /**
     * Gets the total count.
     *
     * @return the total count
     */
    public int getTotalCount() {
        return total_count;
    }

    /**
     * Gets the workflow jobs.
     *
     * @param repo
     *            the repo
     * @return the workflow jobs
     */
    GHWorkflowJob[] getWorkflowJobs(GHRepository repo) {
        for (GHWorkflowJob job : jobs) {
            job.wrapUp(repo);
        }
        return jobs;
    }
}
