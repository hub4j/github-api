package org.kohsuke.github;

import java.util.Iterator;

/**
 * {@link PagedIterable} enhanced to report search result specific information.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class PagedSearchIterable<T> extends PagedIterable<T> {
    private final GitHub root;

    /**
     * As soon as we have any result fetched, it's set here so that we can report the total count.
     */
    private SearchResult<T> result;

    /*package*/ PagedSearchIterable(GitHub root) {
        this.root = root;
    }

    /**
     * Returns the total number of hit, including the results that's not yet fetched.
     */
    public int getTotalCount() {
        populate();
        return result.total_count;
    }

    public boolean isIncomplete() {
        populate();
        return result.incomplete_results;
    }

    private void populate() {
        if (result==null)
            iterator().hasNext();
    }

    /**
     * Adapts {@link Iterator}.
     */
    protected Iterator<T[]> adapt(final Iterator<? extends SearchResult<T>> base) {
        return new Iterator<T[]>() {
            public boolean hasNext() {
                return base.hasNext();
            }

            public T[] next() {
                SearchResult<T> v = base.next();
                if (result==null)   result = v;
                return v.getItems(root);
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
