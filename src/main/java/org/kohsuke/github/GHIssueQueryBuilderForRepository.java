package org.kohsuke.github;

public class GHIssueQueryBuilderForRepository extends GHIssueQueryBuilder {
    private final GHRepository repo;

    public GHIssueQueryBuilderForRepository(final GHRepository repo) {
        super(repo.root());
        this.repo = repo;
    }

    /**
     * Milestone gh issue query builder.
     * <p>
     * The milestone must be either an integer (the milestone number), the string * (issues with any milestone) or the
     * string none (issues without milestone).
     *
     * @param milestone
     *            the milestone
     * @return the gh issue request query builder
     */
    public GHIssueQueryBuilderForRepository milestone(String milestone) {
        req.with("milestone", milestone);
        return this;
    }

    /**
     * Assignee gh issue query builder.
     *
     * @param assignee
     *            the assignee
     * @return the gh issue query builder
     */
    public GHIssueQueryBuilderForRepository assignee(String assignee) {
        req.with("assignee", assignee);
        return this;
    }

    /**
     * Creator gh issue query builder.
     *
     * @param creator
     *            the creator
     * @return the gh issue query builder
     */
    public GHIssueQueryBuilderForRepository creator(String creator) {
        req.with("creator", creator);
        return this;
    }

    /**
     * Mentioned gh issue query builder.
     *
     * @param mentioned
     *            the mentioned
     * @return the gh issue query builder
     */
    public GHIssueQueryBuilderForRepository mentioned(String mentioned) {
        req.with("mentioned", mentioned);
        return this;
    }

    @Override
    public String getApiUrl() {
        return repo.getApiTailUrl("issues");
    }

    @Override
    public PagedIterable<GHIssue> list() {
        return req.withUrlPath(getApiUrl()).toIterable(GHIssue[].class, item -> item.wrap(repo));
    }
}
