package org.kohsuke.github;

// TODO: Auto-generated Javadoc
/**
 * Lists up jobs of a workflow run with some filtering.
 *
 * @author Guillaume Smet
 */
public class GHWorkflowJobQueryBuilder extends GHQueryBuilder<GHWorkflowJob> {
    private final GHRepository repo;

    /**
     * Instantiates a new GH workflow job query builder.
     *
     * @param workflowRun the workflow run
     */
    GHWorkflowJobQueryBuilder(GHWorkflowRun workflowRun) {
        super(workflowRun.getRepository().root());
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

    /**
     * List.
     *
     * @return the paged iterable
     */
    @Override
    public PagedIterable<GHWorkflowJob> list() {
        return new GHWorkflowJobsIterable(repo, req.build());
    }
}
