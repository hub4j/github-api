package org.kohsuke.github;

import java.util.Locale;

/**
 * Search users.
 *
 * @author Kohsuke Kawaguchi
 * @see GitHub#searchUsers()
 */
public class GHUserSearchBuilder extends GHSearchBuilder<GHUser> {
    /*package*/ GHUserSearchBuilder(GitHub root) {
        super(root,UserSearchResult.class);
    }

    /**
     * Search terms.
     */
    public GHUserSearchBuilder q(String term) {
        super.q(term);
        return this;
    }

    public GHUserSearchBuilder type(String v) {
        return q("type:"+v);
    }

    public GHUserSearchBuilder in(String v) {
        return q("in:"+v);
    }

    public GHUserSearchBuilder repos(String v) {
        return q("repos:"+v);
    }

    public GHUserSearchBuilder location(String v) {
        return q("location:"+v);
    }

    public GHUserSearchBuilder language(String v) {
        return q("language:"+v);
    }

    public GHUserSearchBuilder created(String v) {
        return q("created:"+v);
    }

    public GHUserSearchBuilder followers(String v) {
        return q("followers:"+v);
    }

    public GHUserSearchBuilder order(GHDirection v) {
        req.with("order",v);
        return this;
    }

    public GHUserSearchBuilder sort(Sort sort) {
        req.with("sort",sort);
        return this;
    }

    public enum Sort { FOLLOWERS, REPOSITORIES, JOINED }

    private static class UserSearchResult extends SearchResult<GHUser> {
        private GHUser[] items;

        @Override
        /*package*/ GHUser[] getItems(GitHub root) {
            return GHUser.wrap(items,root);
        }
    }

    @Override
    protected String getApiUrl() {
        return "/search/users";
    }
}
