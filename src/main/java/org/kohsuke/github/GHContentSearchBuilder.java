package org.kohsuke.github;

// TODO: Auto-generated Javadoc
/**
 * Search code for {@link GHContent}.
 *
 * @author Kohsuke Kawaguchi
 * @see GitHub#searchContent() GitHub#searchContent()
 */
public class GHContentSearchBuilder extends GHSearchBuilder<GHContent> {
    
    /**
     * Instantiates a new GH content search builder.
     *
     * @param root the root
     */
    GHContentSearchBuilder(GitHub root) {
        super(root, ContentSearchResult.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GHContentSearchBuilder q(String term) {
        super.q(term);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    GHContentSearchBuilder q(String qualifier, String value) {
        super.q(qualifier, value);
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
     * @param v
     *            the v
     * @return the gh content search builder
     * @deprecated use {@link #fork(GHFork)}.
     */
    @Deprecated
    public GHContentSearchBuilder fork(String v) {
        return q("fork", v);
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
        return q("fork", fork.toString());
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
        
        /** The best match. */
        BEST_MATCH, 
 /** The indexed. */
 INDEXED
    }

    private static class ContentSearchResult extends SearchResult<GHContent> {
        private GHContent[] items;

        @Override
        GHContent[] getItems(GitHub root) {
            return items;
        }
    }

    /**
     * Gets the api url.
     *
     * @return the api url
     */
    @Override
    protected String getApiUrl() {
        return "/search/code";
    }
}
