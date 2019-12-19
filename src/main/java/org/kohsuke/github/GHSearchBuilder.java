package org.kohsuke.github;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for various search builders.
 *
 * @param <T>
 *            the type parameter
 * @author Kohsuke Kawaguchi
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
        return new PagedSearchIterable<T>(root) {
            public PagedIterator<T> _iterator(int pageSize) {
                req.set("q", StringUtils.join(terms, " "));
                return new PagedIterator<T>(adapt(req.withUrlPath(getApiUrl()).asIterator(receiverType, pageSize))) {
                    protected void wrapUp(T[] page) {
                        // SearchResult.getItems() should do it
                    }
                };
            }
        };
    }

    /**
     * Gets api url.
     *
     * @return the api url
     */
    protected abstract String getApiUrl();
}
