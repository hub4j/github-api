package org.kohsuke.github;

import org.kohsuke.github.GHWorkflowRun.Conclusion;
import org.kohsuke.github.GHWorkflowRun.Status;

// TODO: Auto-generated Javadoc
/**
 * Lists up workflow runs with some filtering and sorting.
 *
 * @author Guillaume Smet
 * @see GHRepository#queryWorkflowRuns()
 */
public class GHWorkflowRunQueryBuilder extends GHQueryBuilder<GHWorkflowRun> {
    private final GHRepository repo;

    /**
     * Instantiates a new GH workflow run query builder.
     *
     * @param repo
     *            the repo
     */
    GHWorkflowRunQueryBuilder(GHRepository repo) {
        super(repo.root());
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

    /**
     * Conclusion workflow run query builder.
     * <p>
     * The GitHub API is also using the status field to search by conclusion.
     *
     * @param conclusion
     *            the conclusion
     * @return the gh workflow run query builder
     */
    public GHWorkflowRunQueryBuilder conclusion(Conclusion conclusion) {
        req.with("status", conclusion.toString());
        return this;
    }

    /**
     * Created workflow run query builder.
     *
     * @param created
     *            a range following the Query for dates syntax
     *
     * @return the gh workflow run query builder
     * @see <a href=
     *      "https://docs.github.com/en/search-github/getting-started-with-searching-on-github/understanding-the-search-syntax#query-for-dates">Query
     *      for dates</a>
     */
    public GHWorkflowRunQueryBuilder created(String created) {
        req.with("created", created);
        return this;
    }

    /**
     * Head sha workflow run query builder.
     *
     * @param headSha
     *            the head sha
     * @return the gh workflow run query builder
     */
    public GHWorkflowRunQueryBuilder headSha(String headSha) {
        req.with("head_sha", headSha);
        return this;
    }

    /**
     * List.
     *
     * @return the paged iterable
     */
    @Override
    public PagedIterable<GHWorkflowRun> list() {
        return new GHWorkflowRunsIterable(repo, req.withUrlPath(repo.getApiTailUrl("actions/runs")));
    }
}
