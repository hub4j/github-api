package org.kohsuke.github;

/**
 * Lists up pull requests with some filtering and sorting.
 *
 * @author Kohsuke Kawaguchi
 * @see GHRepository#queryPullRequests()
 */
public class GHPullRequestQueryBuilder extends GHQueryBuilder<GHPullRequest> {
    private final GHRepository repo;

    /*package*/ GHPullRequestQueryBuilder(GHRepository repo) {
        super(repo.root);
        this.repo = repo;
    }

    public GHPullRequestQueryBuilder state(GHIssueState state) {
        req.with("state",state);
        return this;
    }

    public GHPullRequestQueryBuilder head(String head) {
        if (head != null && !head.contains(":")) {
            head = repo.getOwnerName() + ":" + head;
        }
        req.with("head",head);
        return this;
    }

    public GHPullRequestQueryBuilder base(String base) {
        req.with("base",base);
        return this;
    }

    public GHPullRequestQueryBuilder sort(Sort sort) {
        req.with("sort",sort);
        return this;
    }

    public enum Sort { CREATED, UPDATED, POPULARITY, LONG_RUNNING }

    public GHPullRequestQueryBuilder direction(GHDirection d) {
        req.with("direction",d);
        return this;
    }

    @Override
    public PagedIterable<GHPullRequest> list() {
        return new PagedIterable<GHPullRequest>() {
            public PagedIterator<GHPullRequest> _iterator(int pageSize) {
                return new PagedIterator<GHPullRequest>(req.asIterator(repo.getApiTailUrl("pulls"), GHPullRequest[].class, pageSize)) {
                    @Override
                    protected void wrapUp(GHPullRequest[] page) {
                        for (GHPullRequest pr : page)
                            pr.wrapUp(repo);
                    }
                };
            }
        };
    }
}
