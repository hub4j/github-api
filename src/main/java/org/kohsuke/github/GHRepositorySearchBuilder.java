package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

// TODO: Auto-generated Javadoc
/**
 * Search repositories.
 *
 * @author Kohsuke Kawaguchi
 * @see GitHub#searchRepositories() GitHub#searchRepositories()
 */
public class GHRepositorySearchBuilder extends GHSearchBuilder<GHRepository> {

    /**
     * The enum Sort.
     */
    public enum Sort {

        /** The forks. */
        FORKS,
        /** The stars. */
        STARS,
        /** The updated. */
        UPDATED
    }

    @SuppressFBWarnings(
            value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD" },
            justification = "JSON API")
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
     * Instantiates a new GH repository search builder.
     *
     * @param root
     *            the root
     */
    GHRepositorySearchBuilder(GitHub root) {
        super(root, RepositorySearchResult.class);
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
     * Searching in forks
     *
     * The default search mode is {@link GHFork#PARENT_ONLY}. In that mode, forks are not included in search results.
     *
     * <p>
     * Passing {@link GHFork#PARENT_AND_FORKS} or {@link GHFork#FORKS_ONLY} will show results from forks, but only if
     * they have more stars than the parent repository.
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
     * {@inheritDoc}
     */
    @Override
    public GHRepositorySearchBuilder q(String term) {
        super.q(term);
        return this;
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
     * Gets the api url.
     *
     * @return the api url
     */
    @Override
    protected String getApiUrl() {
        return "/search/repositories";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    GHRepositorySearchBuilder q(String qualifier, String value) {
        super.q(qualifier, value);
        return this;
    }
}
