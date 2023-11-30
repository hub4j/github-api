package org.kohsuke.github;

// TODO: Auto-generated Javadoc
/**
 * Search repositories.
 *
 * @author Kohsuke Kawaguchi
 * @see GitHub#searchRepositories() GitHub#searchRepositories()
 */
public class GHRepositorySearchBuilder extends GHSearchBuilder<GHRepository> {

    /**
     * Instantiates a new GH repository search builder.
     *
     * @param root
     *            the root
     */
    GHRepositorySearchBuilder(GitHub root) {
        super(root, RepositorySearchResult.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GHRepositorySearchBuilder q(String term) {
        super.q(term);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    GHRepositorySearchBuilder q(String qualifier, String value) {
        super.q(qualifier, value);
        return this;
    }

    /**
     * In gh repository search builder.
     *
     * @param v
     *            the v
     * @return the gh repository search builder
     */
    public GHRepositorySearchBuilder in(String v) {
        return q("in:" + v);
    }

    /**
     * Size gh repository search builder.
     *
     * @param v
     *            the v
     * @return the gh repository search builder
     */
    public GHRepositorySearchBuilder size(String v) {
        return q("size:" + v);
    }

    /**
     * Searching in forks
     *
     * The default search mode is {@link Fork#PARENT_ONLY}. In that mode, forks are not included in search results.
     *
     * <p>
     * Passing {@link Fork#PARENT_AND_FORKS} or {@link Fork#FORKS_ONLY} will show results from forks, but only if they
     * have more stars than the parent repository.
     *
     * <p>
     * IMPORTANT: Regardless of this setting, no search results will ever be returned for forks with equal or fewer
     * stars than the parent repository. Forks with less stars than the parent repository are not included in the index
     * for code searching.
     *
     * @param fork
     *            search mode for forks
     *
     * @return the gh repository search builder
     *
     * @see <a href=
     *      "https://docs.github.com/en/github/searching-for-information-on-github/searching-on-github/searching-in-forks">Searching
     *      in forks</a>
     *
     */
    public GHRepositorySearchBuilder fork(GHFork fork) {
        return q("fork", fork.toString());
    }

    /**
     * Search by repository visibility.
     *
     * @param visibility
     *            repository visibility
     * @return the gh repository search builder
     * @throws GHException
     *             if {@link GHRepository.Visibility#UNKNOWN} is passed. UNKNOWN is a placeholder for unexpected values
     *             encountered when reading data.
     * @see <a href=
     *      "https://docs.github.com/en/github/searching-for-information-on-github/searching-on-github/searching-for-repositories#search-by-repository-visibility">Search
     *      by repository visibility</a>
     */
    public GHRepositorySearchBuilder visibility(GHRepository.Visibility visibility) {
        if (visibility == GHRepository.Visibility.UNKNOWN) {
            throw new GHException(
                    "UNKNOWN is a placeholder for unexpected values encountered when reading data. It cannot be passed as a search parameter.");
        }

        return q("is:" + visibility);
    }

    /**
     * Created gh repository search builder.
     *
     * @param v
     *            the v
     * @return the gh repository search builder
     */
    public GHRepositorySearchBuilder created(String v) {
        return q("created:" + v);
    }

    /**
     * Pushed gh repository search builder.
     *
     * @param v
     *            the v
     * @return the gh repository search builder
     */
    public GHRepositorySearchBuilder pushed(String v) {
        return q("pushed:" + v);
    }

    /**
     * User gh repository search builder.
     *
     * @param v
     *            the v
     * @return the gh repository search builder
     */
    public GHRepositorySearchBuilder user(String v) {
        return q("user:" + v);
    }

    /**
     * Repo gh repository search builder.
     *
     * @param v
     *            the v
     * @return the gh repository search builder
     */
    public GHRepositorySearchBuilder repo(String v) {
        return q("repo:" + v);
    }

    /**
     * Language gh repository search builder.
     *
     * @param v
     *            the v
     * @return the gh repository search builder
     */
    public GHRepositorySearchBuilder language(String v) {
        return q("language:" + v);
    }

    /**
     * Stars gh repository search builder.
     *
     * @param v
     *            the v
     * @return the gh repository search builder
     */
    public GHRepositorySearchBuilder stars(String v) {
        return q("stars:" + v);
    }

    /**
     * Topic gh repository search builder.
     *
     * @param v
     *            the v
     * @return the gh repository search builder
     */
    public GHRepositorySearchBuilder topic(String v) {
        return q("topic:" + v);
    }

    /**
     * Org gh repository search builder.
     *
     * @param v
     *            the v
     * @return the gh repository search builder
     */
    public GHRepositorySearchBuilder org(String v) {
        return q("org:" + v);
    }

    /**
     * Order gh repository search builder.
     *
     * @param v
     *            the v
     * @return the gh repository search builder
     */
    public GHRepositorySearchBuilder order(GHDirection v) {
        req.with("order", v);
        return this;
    }

    /**
     * Sort gh repository search builder.
     *
     * @param sort
     *            the sort
     * @return the gh repository search builder
     */
    public GHRepositorySearchBuilder sort(Sort sort) {
        req.with("sort", sort);
        return this;
    }

    /**
     * The enum Sort.
     */
    public enum Sort {

        /** The stars. */
        STARS,
        /** The forks. */
        FORKS,
        /** The updated. */
        UPDATED
    }

    /**
     * The enum for Fork search mode.
     *
     * @deprecated Kept for backward compatibility. Use {@link GHFork} instead.
     */
    @Deprecated
    public enum Fork {

        /**
         * Search in the parent repository and in forks with more stars than the parent repository.
         *
         * Forks with the same or fewer stars than the parent repository are still ignored.
         */
        PARENT_AND_FORKS("true"),

        /**
         * Search only in forks with more stars than the parent repository.
         *
         * The parent repository is ignored. If no forks have more stars than the parent, no results will be returned.
         */
        FORKS_ONLY("only"),

        /**
         * (Default) Search only the parent repository.
         *
         * Forks are ignored.
         */
        PARENT_ONLY("");

        private String filterMode;

        /**
         * Instantiates a new fork.
         *
         * @param mode
         *            the mode
         */
        Fork(final String mode) {
            this.filterMode = mode;
        }

        /**
         * To string.
         *
         * @return the string
         */
        @Override
        public String toString() {
            return filterMode;
        }
    }

    private static class RepositorySearchResult extends SearchResult<GHRepository> {
        private GHRepository[] items;

        @Override
        GHRepository[] getItems(GitHub root) {
            for (GHRepository item : items) {
            }
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
        return "/search/repositories";
    }
}
