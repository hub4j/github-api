package org.kohsuke.github;

/**
 * Search code for {@link GHContent}.
 *
 * @author Kohsuke Kawaguchi
 * @see GitHub#searchContent() GitHub#searchContent()
 */
public class GHContentSearchBuilder extends GHSearchBuilder<GHContent> {
    GHContentSearchBuilder(GitHub root) {
        super(root, ContentSearchResult.class);
    }

    /**
     * Search terms.
     */
    public GHContentSearchBuilder q(String term) {
        super.q(term);
        return this;
    }

    /**
     * In gh content search builder.
     *
     * @param v
     *            the v
     * @return the gh content search builder
     */
    public GHContentSearchBuilder in(String v) {
        return q("in:" + v);
    }

    /**
     * Language gh content search builder.
     *
     * @param v
     *            the v
     * @return the gh content search builder
     */
    public GHContentSearchBuilder language(String v) {
        return q("language:" + v);
    }

    /**
     * Fork gh content search builder.
     *
     * @param fork
     *            search mode for forks
     *
     * @return the gh content search builder
     *
     * @see <a href=
     *      "https://docs.github.com/en/github/searching-for-information-on-github/searching-on-github/searching-in-forks">Searching
     *      in forks</a>
     */
    public GHContentSearchBuilder fork(GHFork fork) {
        if (GHFork.PARENT_ONLY.equals(fork)) {
            this.terms.removeIf(term -> term.contains("fork:"));
            return this;
        }

        return q("fork:" + fork);
    }

    /**
     * Size gh content search builder.
     *
     * @param v
     *            the v
     * @return the gh content search builder
     */
    public GHContentSearchBuilder size(String v) {
        return q("size:" + v);
    }

    /**
     * Path gh content search builder.
     *
     * @param v
     *            the v
     * @return the gh content search builder
     */
    public GHContentSearchBuilder path(String v) {
        return q("path:" + v);
    }

    /**
     * Filename gh content search builder.
     *
     * @param v
     *            the v
     * @return the gh content search builder
     */
    public GHContentSearchBuilder filename(String v) {
        return q("filename:" + v);
    }

    /**
     * Extension gh content search builder.
     *
     * @param v
     *            the v
     * @return the gh content search builder
     */
    public GHContentSearchBuilder extension(String v) {
        return q("extension:" + v);
    }

    /**
     * User gh content search builder.
     *
     * @param v
     *            the v
     * @return the gh content search builder
     */
    public GHContentSearchBuilder user(String v) {
        return q("user:" + v);
    }

    /**
     * Repo gh content search builder.
     *
     * @param v
     *            the v
     * @return the gh content search builder
     */
    public GHContentSearchBuilder repo(String v) {
        return q("repo:" + v);
    }

    /**
     * Order gh content search builder.
     *
     * @param v
     *            the v
     * @return the gh content search builder
     */
    public GHContentSearchBuilder order(GHDirection v) {
        req.with("order", v);
        return this;
    }

    /**
     * Sort gh content search builder.
     *
     * @param sort
     *            the sort
     * @return the gh content search builder
     */
    public GHContentSearchBuilder sort(GHContentSearchBuilder.Sort sort) {
        if (Sort.BEST_MATCH.equals(sort)) {
            req.remove("sort");
        } else {
            req.with("sort", sort);
        }
        return this;
    }

    /**
     * The enum Sort.
     */
    public enum Sort {
        BEST_MATCH, INDEXED
    }

    private static class ContentSearchResult extends SearchResult<GHContent> {
        private GHContent[] items;

        @Override
        GHContent[] getItems(GitHub root) {
            return items;
        }
    }

    @Override
    protected String getApiUrl() {
        return "/search/code";
    }
}
