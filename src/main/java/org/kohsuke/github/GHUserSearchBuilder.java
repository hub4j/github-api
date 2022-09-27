package org.kohsuke.github;

// TODO: Auto-generated Javadoc
/**
 * Search users.
 *
 * @author Kohsuke Kawaguchi
 * @see GitHub#searchUsers() GitHub#searchUsers()
 */
public class GHUserSearchBuilder extends GHSearchBuilder<GHUser> {
    
    /**
     * Instantiates a new GH user search builder.
     *
     * @param root the root
     */
    GHUserSearchBuilder(GitHub root) {
        super(root, UserSearchResult.class);
    }

    /**
     * Search terms.
     *
     * @param term the term
     * @return the GH user search builder
     */
    public GHUserSearchBuilder q(String term) {
        super.q(term);
        return this;
    }

    /**
     * Type gh user search builder.
     *
     * @param v
     *            the v
     * @return the gh user search builder
     */
    public GHUserSearchBuilder type(String v) {
        return q("type:" + v);
    }

    /**
     * In gh user search builder.
     *
     * @param v
     *            the v
     * @return the gh user search builder
     */
    public GHUserSearchBuilder in(String v) {
        return q("in:" + v);
    }

    /**
     * Repos gh user search builder.
     *
     * @param v
     *            the v
     * @return the gh user search builder
     */
    public GHUserSearchBuilder repos(String v) {
        return q("repos:" + v);
    }

    /**
     * Location gh user search builder.
     *
     * @param v
     *            the v
     * @return the gh user search builder
     */
    public GHUserSearchBuilder location(String v) {
        return q("location:" + v);
    }

    /**
     * Language gh user search builder.
     *
     * @param v
     *            the v
     * @return the gh user search builder
     */
    public GHUserSearchBuilder language(String v) {
        return q("language:" + v);
    }

    /**
     * Created gh user search builder.
     *
     * @param v
     *            the v
     * @return the gh user search builder
     */
    public GHUserSearchBuilder created(String v) {
        return q("created:" + v);
    }

    /**
     * Followers gh user search builder.
     *
     * @param v
     *            the v
     * @return the gh user search builder
     */
    public GHUserSearchBuilder followers(String v) {
        return q("followers:" + v);
    }

    /**
     * Order gh user search builder.
     *
     * @param v
     *            the v
     * @return the gh user search builder
     */
    public GHUserSearchBuilder order(GHDirection v) {
        req.with("order", v);
        return this;
    }

    /**
     * Sort gh user search builder.
     *
     * @param sort
     *            the sort
     * @return the gh user search builder
     */
    public GHUserSearchBuilder sort(Sort sort) {
        req.with("sort", sort);
        return this;
    }

    /**
     * The enum Sort.
     */
    public enum Sort {
        
        /** The followers. */
        FOLLOWERS, 
 /** The repositories. */
 REPOSITORIES, 
 /** The joined. */
 JOINED
    }

    private static class UserSearchResult extends SearchResult<GHUser> {
        private GHUser[] items;

        @Override
        GHUser[] getItems(GitHub root) {
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
        return "/search/users";
    }
}
