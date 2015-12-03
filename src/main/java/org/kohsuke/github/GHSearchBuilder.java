package org.kohsuke.github;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for various search builders.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class GHSearchBuilder<T> {
    protected final GitHub root;
    protected final Requester req;
    protected final List<String> terms = new ArrayList<String>();

    /**
     * Data transfer object that receives the result of search.
     */
    private final Class<? extends SearchResult<T>> receiverType;

    /*package*/ GHSearchBuilder(GitHub root, Class<? extends SearchResult<T>> receiverType) {
        this.root = root;
        this.req = root.retrieve();
        this.receiverType = receiverType;
    }

    /**
     * Search terms.
     */
    public GHSearchBuilder q(String term) {
        terms.add(term);
        return this;
    }

    /**
     * Performs the search.
     */
    public PagedSearchIterable<T> list() {
        return new PagedSearchIterable<T>(root) {
            public PagedIterator<T> _iterator(int pageSize) {
                req.set("q", StringUtils.join(terms, " "));
                return new PagedIterator<T>(adapt(req.asIterator(getApiUrl(), receiverType, pageSize))) {
                    protected void wrapUp(T[] page) {
                        // SearchResult.getItems() should do it
                    }
                };
            }
        };
    }

    protected abstract String getApiUrl();
}
