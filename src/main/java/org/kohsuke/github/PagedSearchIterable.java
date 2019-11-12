package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Iterator;

/**
 * {@link PagedIterable} enhanced to report search result specific information.
 *
 * @author Kohsuke Kawaguchi
 */
@SuppressFBWarnings(value = {"UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD",
    "UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR"}, justification = "Constructed by JSON API")
public abstract class PagedSearchIterable<T> extends PagedIterable<T> {
    /**
     * As soon as we have any result fetched, it's set here so that we can report the total count.
     */
    private SearchResult<T> result;

    /*package*/ PagedSearchIterable(GitHub root) {
        this.root = root;
    }

    @Override
    public PagedSearchIterable<T> withPageSize(int size) {
        return (PagedSearchIterable<T>)super.withPageSize(size);
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
