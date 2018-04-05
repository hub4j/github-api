package org.kohsuke.github;

/**
 * Search code for {@link GHContent}.
 *
 * @author Kohsuke Kawaguchi
 * @see GitHub#searchContent()
 */
public class GHContentSearchBuilder extends GHSearchBuilder<GHContent> {
    /*package*/ GHContentSearchBuilder(GitHub root) {
        super(root,ContentSearchResult.class);
    }

    /**
     * Search terms.
     */
    public GHContentSearchBuilder q(String term) {
        super.q(term);
        return this;
    }

    public GHContentSearchBuilder in(String v) {
        return q("in:"+v);
    }

    public GHContentSearchBuilder language(String v) {
        return q("language:"+v);
    }

    public GHContentSearchBuilder fork(String v) {
        return q("fork:"+v);
    }

    public GHContentSearchBuilder size(String v) {
        return q("size:"+v);
    }

    public GHContentSearchBuilder path(String v) {
        return q("path:"+v);
    }

    public GHContentSearchBuilder filename(String v) {
        return q("filename:"+v);
    }

    public GHContentSearchBuilder extension(String v) {
        return q("extension:"+v);
    }

    public GHContentSearchBuilder user(String v) {
        return q("user:"+v);
    }


    public GHContentSearchBuilder repo(String v) {
        return q("repo:"+v);
    }

    private static class ContentSearchResult extends SearchResult<GHContent> {
        private GHContent[] items;

        @Override
        /*package*/ GHContent[] getItems(GitHub root) {
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
