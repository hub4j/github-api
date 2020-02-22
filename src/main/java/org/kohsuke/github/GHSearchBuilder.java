package org.kohsuke.github;

import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Base class for various search builders.
 *
 * @param <T>
 *            the type parameter
 */
public abstract class GHSearchBuilder<T> extends GHQueryBuilder<T> {
    protected final List<String> terms = new ArrayList<String>();

    /**
     * Data transfer object that receives the result of search.
     */
    private final Class<? extends SearchResult<T>> receiverType;

    GHSearchBuilder(GitHub root, Class<? extends SearchResult<T>> receiverType) {
        super(root);
        this.receiverType = receiverType;
    }

    /**
     * Search terms.
     *
     * @param term
     *            the term
     * @return the gh query builder
     */
    public GHQueryBuilder<T> q(String term) {
        terms.add(term);
        return this;
    }

    /**
     * Performs the search.
     */
    @Override
    public PagedSearchIterable<T> list() {

        req.set("q", StringUtils.join(terms, " "));
        try {
            final GitHubRequest baseRequest = req.build();
            return new PagedSearchIterable<T>(root) {
                @Nonnull
                public PagedIterator<T> _iterator(int pageSize) {
                    return new PagedIterator<T>(adapt(GitHubPageIterator.create(root.getClient(),
                            receiverType,
                            baseRequest.toBuilder().withUrlPath(getApiUrl()).withPageSize(pageSize)))) {
                        protected void wrapUp(T[] page) {
                            // PagedSearchIterable
                            // SearchResult.getItems() should do it
                        }
                    };
                }
            };
        } catch (MalformedURLException e) {
            throw new GHException("", e);
        }
    }

    /**
     * Gets api url.
     *
     * @return the api url
     */
    protected abstract String getApiUrl();
}
