package org.kohsuke.github;

// TODO: Auto-generated Javadoc
/**
 * Lists up pull requests with some filtering and sorting.
 *
 * @author Kohsuke Kawaguchi
 * @see GHRepository#queryPullRequests() GHRepository#queryPullRequests()
 */
public class GHPullRequestQueryBuilder extends GHQueryBuilder<GHPullRequest> {
    private final GHRepository repo;

    /**
     * Instantiates a new GH pull request query builder.
     *
     * @param repo
     *            the repo
     */
    GHPullRequestQueryBuilder(GHRepository repo) {
        super(repo.root());
        this.repo = repo;
    }

    /**
     * State gh pull request query builder.
     *
     * @param state
     *            the state
     * @return the gh pull request query builder
     */
    public GHPullRequestQueryBuilder state(GHIssueState state) {
        req.with("state", state);
        return this;
    }

    /**
     * Head gh pull request query builder.
     *
     * @param head
     *            the head
     * @return the gh pull request query builder
     */
    public GHPullRequestQueryBuilder head(String head) {
        if (head != null && !head.contains(":")) {
            head = repo.getOwnerName() + ":" + head;
        }
        req.with("head", head);
        return this;
    }

    /**
     * Base gh pull request query builder.
     *
     * @param base
     *            the base
     * @return the gh pull request query builder
     */
    public GHPullRequestQueryBuilder base(String base) {
        req.with("base", base);
        return this;
    }

    /**
     * Sort gh pull request query builder.
     *
     * @param sort
     *            the sort
     * @return the gh pull request query builder
     */
    public GHPullRequestQueryBuilder sort(Sort sort) {
        req.with("sort", sort);
        return this;
    }

    /**
     * The enum Sort.
     */
    public enum Sort {

        /** The created. */
        CREATED,
        /** The updated. */
        UPDATED,
        /** The popularity. */
        POPULARITY,
        /** The long running. */
        LONG_RUNNING
    }

    /**
     * Direction gh pull request query builder.
     *
     * @param d
     *            the d
     * @return the gh pull request query builder
     */
    public GHPullRequestQueryBuilder direction(GHDirection d) {
        req.with("direction", d);
        return this;
    }

    /**
     * List.
     *
     * @return the paged iterable
     */
    @Override
    public PagedIterable<GHPullRequest> list() {
        return req.withUrlPath(repo.getApiTailUrl("pulls"))
                .toIterable(GHPullRequest[].class, item -> item.wrapUp(repo));
    }
}
