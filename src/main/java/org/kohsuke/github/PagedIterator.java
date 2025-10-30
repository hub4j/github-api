package org.kohsuke.github;

import java.util.Iterator;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * Iterator over a paginated data source. Iterates of the content items of each page, automatically requesting new pages
 * as needed.
 * <p>
 * Aside from the normal iterator operation, this method exposes {@link #nextPage()} and {@link #nextPageArray()} that
 * allows the caller to retrieve entire pages.
 *
 * This class is not thread-safe. Any one instance should only be called from a single thread.
 *
 * @author Kohsuke Kawaguchi
 * @param <T>
 *            the type parameter
 */
@Deprecated
public class PagedIterator<T> implements Iterator<T> {

    private final PaginatedEndpointItems<? extends GitHubPage<T>, T> endpointIterator;

    /**
     * Instantiates a new paged iterator.
     *
     * @param endpointIterator
     *            the base
     */
    PagedIterator(PaginatedEndpointItems<? extends GitHubPage<T>, T> endpointIterator) {
        this.endpointIterator = endpointIterator;
    }

    public boolean hasNext() {
        return endpointIterator.hasNext();
    }

    public T next() {
        return endpointIterator.next();
    }

    /**
     * Get the next page of items.
     *
     * @return a list of the next page of items.
     * @deprecated use PagedIterable.pages().
     */
    @Deprecated
    public List<T> nextPage() {
        return endpointIterator.nextPage();
    }
}
