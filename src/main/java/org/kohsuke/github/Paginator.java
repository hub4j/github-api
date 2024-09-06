package org.kohsuke.github;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
/**
 * Iterator over a paginated data source. Iterates over the content items of each page, automatically requesting new
 * pages as needed.
 * <p>
 * Equivalent to {@link PagedIterator} but with support for bidirectional movement and jumping to first, last or any
 * specific page. This class is not thread-safe. Any one instance should only be called from a single thread.
 *
 * @author Anuj Hydrabadi
 * @param <T>
 *            the type parameter
 */
public class Paginator<T> implements NavigableIterator<T> {
    /**
     * The base.
     */
    @Nonnull
    protected final NavigablePageIterator<T[]> base;

    @CheckForNull
    private final Consumer<T> itemInitializer;

    /**
     * Current batch of items. This field, long with {@link #nextItemIndex} maintains the state of the class. If
     * {@link #nextItemIndex} moves out of bounds of this array, the next/previous batch of data is fetched and replaced
     * here.
     */
    private T[] currentPage;

    /**
     * The index of the next item to be fetched from the {@link #currentPage}.
     */
    private int nextItemIndex;

    /**
     * Instantiates a new paginator.
     *
     * @param base
     *            the base
     * @param itemInitializer
     *            the item initializer
     */
    Paginator(@Nonnull NavigablePageIterator<T[]> base, @CheckForNull Consumer<T> itemInitializer) {
        this.base = base;
        this.itemInitializer = itemInitializer;
    }

    /**
     * This method initializes items with local data after they are fetched. It is up to the implementer to decide what
     * local data to apply.
     *
     * @param page
     *            the page of items to be initialized
     */
    protected void wrapUp(T[] page) {
        if (itemInitializer != null) {
            for (T item : page) {
                itemInitializer.accept(item);
            }
        }
    }

    /**
     * @return true if there is any data on the next index.
     */
    @Override
    public boolean hasNext() {
        if (currentPage != null) {
            return nextItemIndex < currentPage.length || base.hasNext();
        } else {
            fetchNext();
            return currentPage.length != 0;
        }
    }

    /**
     * Increments the pointer and returns the next item.
     *
     * @return the item on the next index if present
     * @throws NoSuchElementException
     *             if not present
     */
    @Override
    public T next() {
        if (!hasNext())
            throw new NoSuchElementException();
        if (currentPage != null && nextItemIndex < currentPage.length)
            return currentPage[nextItemIndex++];
        fetchNext();
        if (currentPage == null || currentPage.length == 0) {
            throw new NoSuchElementException();
        }
        nextItemIndex = 0;
        return currentPage[nextItemIndex++];
    }

    /**
     * @return true if there is any data on the previous index.
     */
    @Override
    public boolean hasPrevious() {
        return (currentPage != null && nextItemIndex > 0) || base.hasPrevious();
    }

    /**
     * Decrements the pointer and returns the previous item.
     *
     * @return the item on the previous index if present
     * @throws NoSuchElementException
     *             if not present
     */
    @Override
    public T previous() {
        if (!hasPrevious())
            throw new NoSuchElementException();
        if (currentPage != null && nextItemIndex > 0)
            return currentPage[--nextItemIndex];
        fetchPrevious();
        if (currentPage == null || currentPage.length == 0) {
            throw new NoSuchElementException();
        }
        nextItemIndex = currentPage.length - 1;
        return currentPage[nextItemIndex];
    }

    /**
     * Returns the first item and sets the pointer after that item.
     *
     * @return the first item.
     */
    @Override
    public T first() {
        nextItemIndex = 0;
        T[] result = base.first();
        wrapUp(result);
        currentPage = result;
        if (currentPage.length == 0) {
            throw new NoSuchElementException();
        }
        return currentPage[nextItemIndex++];
    }

    /**
     * Gets the entire first page of data. Sets the pointer to the end of the first page.
     *
     * @return the list
     */
    T[] firstPageArray() {
        T[] result = base.first();
        wrapUp(result);
        currentPage = result;
        nextItemIndex = currentPage.length;
        return currentPage;
    }

    /**
     * Gets the entire first page of data. Sets the pointer to the end of the first page.
     *
     * @return the list
     */
    public List<T> firstPageList() {
        return Arrays.asList(firstPageArray());
    }

    /**
     * Returns the last item and sets the pointer after that item.
     *
     * @return the last item.
     */
    @Override
    public T last() {
        T[] result = base.last();
        wrapUp(result);
        currentPage = result;
        nextItemIndex = currentPage.length - 1;
        if (currentPage.length == 0) {
            throw new NoSuchElementException();
        }
        return currentPage[nextItemIndex++];
    }

    /**
     * Gets the entire last page of data. Sets the pointer to the end of the last page.
     *
     * @return the list
     */
    T[] lastPageArray() {
        T[] result = base.last();
        wrapUp(result);
        currentPage = result;
        nextItemIndex = currentPage.length;
        return currentPage;
    }

    /**
     * Gets the entire last page of data. Sets the pointer to the end of the last page.
     *
     * @return the list
     */
    public List<T> lastPageList() {
        return Arrays.asList(lastPageArray());
    }

    /**
     * @return total number of pages
     */
    public int totalPages() {
        if (!hasPrevious() && !hasNext()) {
            return 0;
        }
        return base.totalCount();
    }

    /**
     * @return the current page number
     */
    public int currentPage() {
        return base.currentPage();
    }

    /**
     * Gets the next page worth of data. Sets the pointer to the end of that page.
     *
     * @return the list
     */
    public List<T> nextPage() {
        return Arrays.asList(nextPageArray());
    }

    /**
     * Gets the next page worth of data. Sets the pointer to the end of that page.
     *
     * @return the list
     */
    @Nonnull
    T[] nextPageArray() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        next();

        // Current should never be null after fetch
        Objects.requireNonNull(currentPage);
        T[] r = currentPage;
        if (nextItemIndex != 0) {
            r = Arrays.copyOfRange(r, nextItemIndex - 1, r.length);
        }
        nextItemIndex = currentPage.length;
        return r;
    }

    /**
     * Gets the previous page worth of data. Sets the pointer to the start of that page.
     *
     * @return the list
     */
    public List<T> previousPage() {
        return Arrays.asList(previousPageArray());
    }

    /**
     * Gets the previous page worth of data. Sets the pointer to the start of that page.
     *
     * @return the list
     */
    @Nonnull
    T[] previousPageArray() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        previous();
        // Current should never be null after fetch
        Objects.requireNonNull(currentPage);
        T[] r = currentPage;
        if (nextItemIndex != currentPage.length - 1) {
            r = Arrays.copyOfRange(r, 0, nextItemIndex + 1);
        }
        nextItemIndex = 0;
        return r;
    }

    /**
     * Clears out the cached data and updates it with the underlying data source. The position of the pointer stays the
     * same, though the data it is pointing to may have changed.
     */
    public void refresh() {
        base.refresh();
        currentPage = null;
    }

    /**
     * Jump to a particular page.
     *
     * @param page
     *            the page to jump to.
     * @return the paginator object to support fluent method chaining.
     * @throws NoSuchElementException
     *             if the page does not exist.
     */
    public Paginator<T> jumpToPage(int page) {
        currentPage = base.jumpToPage(page);
        nextItemIndex = 0;
        return this;
    }

    /**
     * Called at the start of the {@link #next()} method if we have reached the end of {@link #currentPage}. Updates the
     * current page with the next page data. Does not update the pointer.
     */
    private void fetchNext() {
        if (base.hasNext()) {
            T[] result = Objects.requireNonNull(base.next());
            wrapUp(result);
            currentPage = result;
        }
    }

    /**
     * Called at the start of the {@link #previous()} method if we are at the start of {@link #currentPage}. Updates the
     * current page with the previous page data. Does not update the pointer.
     */
    private void fetchPrevious() {
        if (base.hasPrevious()) {
            T[] result = Objects.requireNonNull(base.previous());
            wrapUp(result);
            currentPage = result;
        }
    }
}
