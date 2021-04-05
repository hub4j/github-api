package org.kohsuke.github;

import java.net.MalformedURLException;

/**
 * Lists up jobs of a workflow run with some filtering.
 *
 * @author Guillaume Smet
 */
public class GHWorkflowJobQueryBuilder extends GHQueryBuilder<GHWorkflowJob> {
    private final GHRepository repo;

    GHWorkflowJobQueryBuilder(GHWorkflowRun workflowRun) {
        super(workflowRun.getRepository().root);
        this.repo = workflowRun.getRepository();
        req.withUrlPath(repo.getApiTailUrl("actions/runs"), String.valueOf(workflowRun.getId()), "jobs");
    }

    /**
     * Apply a filter to only return the jobs of the most recent execution of the workflow run.
     *
     * @return the workflow run job query builder
     */
    public GHWorkflowJobQueryBuilder latest() {
        req.with("filter", "latest");
        return this;
    }

    /**
     * Apply a filter to return jobs from all executions of this workflow run.
     *
     * @return the workflow run job run query builder
     */
    public GHWorkflowJobQueryBuilder all() {
        req.with("filter", "all");
        return this;
    }

    @Override
    public PagedIterable<GHWorkflowJob> list() {
        try {
            return new GHWorkflowJobsIterable(repo, req.build());
        } catch (MalformedURLException e) {
            throw new GHException(e.getMessage(), e);
        }
    }
}
