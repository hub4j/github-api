package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

// TODO: Auto-generated Javadoc
/**
 * {@link PagedIterable} enhanced to report search result specific information.
 *
 * @author Kohsuke Kawaguchi
 * @param <T>
 *            the type parameter
 */
@SuppressFBWarnings(
        value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD",
                "UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR" },
        justification = "Constructed by JSON API")
public class PagedSearchIterable<T> extends PagedIterable<T> {

    private final PaginatedEndpoint<? extends SearchResult<T>, T> searchPaginatedEndpoint;
    /**
     * Instantiates a new paged search iterable.
     */
    <Result extends SearchResult<T>> PagedSearchIterable(PaginatedEndpoint<Result, T> paginatedEndpoint) {
        super(paginatedEndpoint);
        this.searchPaginatedEndpoint = paginatedEndpoint;
    }

    /**
     * Returns the total number of hit, including the results that's not yet fetched.
     *
     * @return the total count
     */
    @Deprecated
    public int getTotalCount() {
        // populate();
        return searchPaginatedEndpoint.pages().peek().totalCount;
    }

    /**
     * Is incomplete boolean.
     *
     * @return the boolean
     */
    @Deprecated
    public boolean isIncomplete() {
        // populate();
        return searchPaginatedEndpoint.pages().peek().incompleteResults;
    }

    /**
     * With page size.
     *
     * @param size
     *            the size
     * @return the paged search iterable
     */
    @Override
    public PagedSearchIterable<T> withPageSize(int size) {
        return (PagedSearchIterable<T>) super.withPageSize(size);
    }

    @Override
    PaginatedEndpointItems<? extends SearchResult<T>, T> items() {
        return new PaginatedEndpointItems<>(pages());
    }

    @Override
    PaginatedEndpointPages<? extends SearchResult<T>, T> pages() {
        return searchPaginatedEndpoint.pages();
    }
}
