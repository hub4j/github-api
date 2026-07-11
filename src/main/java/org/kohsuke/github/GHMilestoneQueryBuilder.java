package org.kohsuke.github;

/**
 * Lists up milestones with filtering and sorting.
 *
 * @see GHRepository#queryMilestones() GHRepository#queryMilestones()
 */
public class GHMilestoneQueryBuilder extends GHQueryBuilder<GHMilestone> {
    /**
     * The enum Sort.
     */
    public enum Sort {

        /** Sort by completeness (percentage of issues closed). */
        COMPLETENESS,
        /** Sort by due date. */
        DUE_ON
    }

    private final GHRepository repo;

    /**
     * Instantiates a new GH milestone query builder.
     *
     * @param repo
     *            the repo
     */
    GHMilestoneQueryBuilder(GHRepository repo) {
        super(repo.root());
        this.repo = repo;
    }

    /**
     * Direction gh milestone query builder.
     *
     * @param d
     *            the d
     * @return the gh milestone query builder
     */
    public GHMilestoneQueryBuilder direction(GHDirection d) {
        req.with("direction", d);
        return this;
    }

    /**
     * List.
     *
     * @return the paged iterable
     */
    @Override
    public PagedIterable<GHMilestone> list() {
        return req.withUrlPath(repo.getApiTailUrl("milestones"))
                .toIterable(GHMilestone[].class, item -> item.lateBind(repo));
    }

    /**
     * Sort gh milestone query builder.
     *
     * @param sort
     *            the sort
     * @return the gh milestone query builder
     */
    public GHMilestoneQueryBuilder sort(Sort sort) {
        req.with("sort", sort);
        return this;
    }

    /**
     * State gh milestone query builder.
     *
     * @param state
     *            the state
     * @return the gh milestone query builder
     */
    public GHMilestoneQueryBuilder state(GHIssueState state) {
        req.with("state", state);
        return this;
    }
}
