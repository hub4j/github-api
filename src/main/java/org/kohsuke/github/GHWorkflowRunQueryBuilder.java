package org.kohsuke.github;

import org.kohsuke.github.GHWorkflowRun.Status;

import java.net.MalformedURLException;

/**
 * Lists up workflow runs with some filtering and sorting.
 *
 * @author Guillaume Smet
 * @see GHRepository#queryWorkflowRuns()
 */
public class GHWorkflowRunQueryBuilder extends GHQueryBuilder<GHWorkflowRun> {
    private final GHRepository repo;

    GHWorkflowRunQueryBuilder(GHRepository repo) {
        super(repo.root);
        this.repo = repo;
    }

    /**
     * Actor workflow run query builder.
     *
     * @param actor
     *            the actor
     * @return the gh workflow run query builder
     */
    public GHWorkflowRunQueryBuilder actor(String actor) {
        req.with("actor", actor);
        return this;
    }

    /**
     * Actor workflow run query builder.
     *
     * @param actor
     *            the actor
     * @return the gh workflow run query builder
     */
    public GHWorkflowRunQueryBuilder actor(GHUser actor) {
        req.with("actor", actor.getLogin());
        return this;
    }

    /**
     * Branch workflow run query builder.
     *
     * @param branch
     *            the branch
     * @return the gh workflow run query builder
     */
    public GHWorkflowRunQueryBuilder branch(String branch) {
        req.with("branch", branch);
        return this;
    }

    /**
     * Event workflow run query builder.
     *
     * @param event
     *            the event
     * @return the gh workflow run query builder
     */
    public GHWorkflowRunQueryBuilder event(GHEvent event) {
        req.with("event", event.symbol());
        return this;
    }

    /**
     * Event workflow run query builder.
     *
     * @param event
     *            the event
     * @return the gh workflow run query builder
     */
    public GHWorkflowRunQueryBuilder event(String event) {
        req.with("event", event);
        return this;
    }

    /**
     * Status workflow run query builder.
     *
     * @param status
     *            the status
     * @return the gh workflow run query builder
     */
    public GHWorkflowRunQueryBuilder status(Status status) {
        req.with("status", status.toString());
        return this;
    }

    @Override
    public PagedIterable<GHWorkflowRun> list() {
        try {
            return new GHWorkflowRunsIterable(repo, req.withUrlPath(repo.getApiTailUrl("actions/runs")).build());
        } catch (MalformedURLException e) {
            throw new GHException(e.getMessage(), e);
        }
    }
}
