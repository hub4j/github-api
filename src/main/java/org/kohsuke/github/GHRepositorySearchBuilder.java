package org.kohsuke.github;

/**
 * Search repositories.
 *
 * @author Kohsuke Kawaguchi
 * @see GitHub#searchRepositories()
 */
public class GHRepositorySearchBuilder extends GHSearchBuilder<GHRepository> {
    /*package*/ GHRepositorySearchBuilder(GitHub root) {
        super(root,RepositorySearchResult.class);
    }

    /**
     * Search terms.
     */
    public GHRepositorySearchBuilder q(String term) {
        super.q(term);
        return this;
    }

    public GHRepositorySearchBuilder in(String v) {
        return q("in:"+v);
    }

    public GHRepositorySearchBuilder topic(String v) {
        return q("topic:"+v);
    }

    public GHRepositorySearchBuilder size(String v) {
        return q("size:"+v);
    }

    public GHRepositorySearchBuilder forks(String v) {
        return q("forks:"+v);
    }

    public GHRepositorySearchBuilder created(String v) {
        return q("created:"+v);
    }

    public GHRepositorySearchBuilder pushed(String v) {
        return q("pushed:"+v);
    }

    public GHRepositorySearchBuilder user(String v) {
        return q("user:"+v);
    }

    public GHRepositorySearchBuilder repo(String v) {
        return q("repo:"+v);
    }

    public GHRepositorySearchBuilder language(String v) {
        return q("language:"+v);
    }

    public GHRepositorySearchBuilder stars(String v) {
        return q("stars:"+v);
    }

    public GHRepositorySearchBuilder order(GHDirection v) {
        req.with("order",v);
        return this;
    }

    public GHRepositorySearchBuilder sort(Sort sort) {
        req.with("sort",sort);
        return this;
    }

    public GHRepositorySearchBuilder page(int startPage) {
    	if(startPage>1)
    		req.with("page",startPage);
        return this;
    }

    public enum Sort { STARS, FORKS, UPDATED }

    private static class RepositorySearchResult extends SearchResult<GHRepository> {
        private GHRepository[] items;

        @Override
        /*package*/ GHRepository[] getItems(GitHub root) {
            for (GHRepository item : items)
                item.wrap(root);
            return items;
        }
    }

    @Override
    protected String getApiUrl() {
        return "/search/repositories";
    }
}
