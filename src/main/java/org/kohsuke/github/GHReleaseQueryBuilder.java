package org.kohsuke.github;

/**
 * Builds up the query for listing releases.
 *
 * @author eyalfoni
 * @see GHRepository#queryReleases()
 */
public class GHReleaseQueryBuilder {
    private final Requester req;
    private final GHRepository repo;

    /**
     * Instantiates a new GH release query builder.
     *
     * @param repo
     *            the repo
     */
    GHReleaseQueryBuilder(GHRepository repo) {
        this.repo = repo;
        this.req = repo.root().createRequest(); // requester to build up
    }

    /**
     * Page gh release query builder.
     *
     * @param page
     *            the page
     * @return the gh release query builder
     */
    public GHReleaseQueryBuilder page(int page) {
        req.with("page", page);
        return this;
    }

    /**
     * Page size gh release query builder.
     *
     * @param pageSize
     *            the page size
     * @return the gh release query builder
     */
    public GHReleaseQueryBuilder pageSize(int pageSize) {
        req.with("per_page", pageSize);
        return this;
    }

    /**
     * Lists up the releases with the criteria built so far.
     *
     * @return the paged iterable
     */
    public PagedIterable<GHRelease> list() {
        return req.withUrlPath(repo.getApiTailUrl("releases")).toIterable(GHRelease[].class, item -> item.wrap(repo));
    }
}
