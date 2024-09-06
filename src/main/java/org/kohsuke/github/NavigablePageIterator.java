package org.kohsuke.github;

/**
 * Extension to {@link NavigableIterator} for iterating over a list of pages of data.
 *
 * @author Anuj Hydrabadi
 * @param <E>
 *            the type of the page of the data.
 */
public interface NavigablePageIterator<E> extends NavigableIterator<E> {

    /**
     * Get the total number of pages.
     *
     * @return the total number of pages
     */
    int totalCount();

    /**
     * Get the current page number.
     *
     * @return the current page number.
     */
    int currentPage();

    /**
     * Jump the cursor to a specific page.
     *
     * @param page
     *            the page number
     * @return the page.
     */
    E jumpToPage(int page);

    /**
     * Refresh stale data. Needed when the underlying data has changed but is not reflected due to caching in the
     * implementation.
     */
    void refresh();
}
