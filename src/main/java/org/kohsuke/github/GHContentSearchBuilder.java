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
     * @param v
     *            the v
     * @return the gh content search builder
     */
    public GHContentSearchBuilder fork(String v) {
        return q("fork:" + v);
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

    private static class ContentSearchResult extends SearchResult<GHContent> {
        private GHContent[] items;

        @Override
        GHContent[] getItems(GitHub root) {
            for (GHContent item : items)
                item.wrap(root);
            return items;
        }
    }

    @Override
    protected String getApiUrl() {
        return "/search/code";
    }
}
