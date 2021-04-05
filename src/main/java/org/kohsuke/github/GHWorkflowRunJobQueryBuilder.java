package org.kohsuke.github;

import java.net.MalformedURLException;

/**
 * Lists up jobs of a workflow run with some filtering.
 *
 * @author Guillaume Smet
 */
public class GHWorkflowRunJobQueryBuilder extends GHQueryBuilder<GHWorkflowRunJob> {
    private final GHRepository repo;

    GHWorkflowRunJobQueryBuilder(GHWorkflowRun workflowRun) {
        super(workflowRun.getRepository().root);
        this.repo = workflowRun.getRepository();
        req.withUrlPath(repo.getApiTailUrl("actions/runs"), String.valueOf(workflowRun.getId()), "jobs");
    }

    /**
     * Apply a filter to only return the jobs of the most recent execution of the workflow run.
     *
     * @return the workflow run job query builder
     */
    public GHWorkflowRunJobQueryBuilder latest() {
        req.with("filter", "latest");
        return this;
    }

    /**
     * Apply a filter to return jobs from all executions of this workflow run.
     *
     * @return the workflow run job run query builder
     */
    public GHWorkflowRunJobQueryBuilder all() {
        req.with("filter", "all");
        return this;
    }

    @Override
    public PagedIterable<GHWorkflowRunJob> list() {
        try {
            return new GHWorkflowRunJobsIterable(repo, req.build());
        } catch (MalformedURLException e) {
            throw new GHException(e.getMessage(), e);
        }
    }
}
