package org.kohsuke.github;

import java.util.*;

/**
 * This class is not thread-safe. Any one instance should only be called from a single thread.
 */
class PaginatedEndpointItems<Page extends GitHubPage<Item>, Item> implements Iterator<Item> {

    /**
     * Current batch of items. Each time {@link #next()} is called the next item in this array will be returned. After
     * the last item of the array is returned, when {@link #next()} is called again, a new page of items will be fetched
     * and iterating will continue from the first item in the new page.
     *
     * @see #fetchNext() {@link #fetchNext()} for details on how this field is used.
     */
    private Page currentPage;

    /**
     * The index of the next item on the page, the item that will be returned when {@link #next()} is called.
     *
     * @see #fetchNext() {@link #fetchNext()} for details on how this field is used.
     */
    private int nextItemIndex;

    private final PaginatedEndpointPages<Page, Item> pageIterator;

    PaginatedEndpointItems(PaginatedEndpointPages<Page, Item> pageIterator) {
        this.pageIterator = pageIterator;
    }

    /**
     * Get the current page.
     *
     * If not previously fetched, will attempt fetch the first page. Will still return the last page even after
     * hasNext() returns false.
     */
    public Page getCurrentPage() {
        if (currentPage == null) {
            peek();
        }
        return currentPage;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasNext() {
        return peek() != null;
    }

    /**
     * {@inheritDoc}
     */
    public Item next() {
        Item result = peek();
        if (result == null)
            throw new NoSuchElementException();
        nextItemIndex++;
        return result;
    }

    /**
     * Gets the next page worth of data.
     *
     * @return the list
     */
    @Deprecated
    public List<Item> nextPage() {
        // if we have not fetched any pages yet, always fetch.
        // If we have fetched at least one page, check hasNext()
        if (currentPage == null) {
            peek();
        } else if (!hasNext()) {
            throw new NoSuchElementException();
        }

        // Current should never be null after fetch
        Objects.requireNonNull(currentPage);
        Item[] r = currentPage.getItems();
        if (nextItemIndex != 0) {
            r = Arrays.copyOfRange(r, nextItemIndex, r.length);
        }
        nextItemIndex = currentPage.getItems().length;
        return Arrays.asList(r);
    }

    /**
     *
     * @return
     */
    public Item peek() {
        Item result = lookupItem();
        if (result == null && pageIterator.hasNext()) {
            result = fetchNext();
        }
        return result;
    }

    /**
     * Fetch is called at the start of {@link #next()} or {@link #hasNext()} to fetch another page of data if it is
     * needed and available.
     * <p>
     * If there is no current page yet (at the start of iterating), a page is fetched. If {@link #nextItemIndex} points
     * to an item in the current page array, the state is valid - no more work is needed. If {@link #nextItemIndex} is
     * greater than the last index in the current page array, the method checks if there is another page of data
     * available.
     * </p>
     * <p>
     * If there is another page, get that page of data and reset the check {@link #nextItemIndex} to the start of the
     * new page.
     * </p>
     * <p>
     * If no more pages are available, leave the page and index unchanged. In this case, {@link #hasNext()} will return
     * {@code false} and {@link #next()} will throw an exception.
     * </p>
     */
    private Item fetchNext() {
        // On first call, always get next page (may be empty array)
        currentPage = Objects.requireNonNull(pageIterator.next());
        nextItemIndex = 0;
        return lookupItem();
    }

    private Item lookupItem() {
        return currentPage != null && currentPage.getItems().length > nextItemIndex
                ? currentPage.getItems()[nextItemIndex]
                : null;
    }
}
