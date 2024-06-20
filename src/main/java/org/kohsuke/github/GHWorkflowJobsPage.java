package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

// TODO: Auto-generated Javadoc
/**
 * Represents the one page of jobs result when listing jobs from a workflow run.
 */
@SuppressFBWarnings(value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD" },
        justification = "JSON API")
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
