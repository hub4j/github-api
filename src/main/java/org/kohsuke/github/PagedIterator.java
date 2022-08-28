package org.kohsuke.github;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Iterator over a paginated data source. Iterates of the content items of each page, automatically requesting new pages
 * as needed.
 * <p>
 * Aside from the normal iterator operation, this method exposes {@link #nextPage()} and {@link #nextPageArray()} that
 * allows the caller to retrieve entire pages.
 *
 * This class is not thread-safe. Any one instance should only be called from a single thread.
 *
 * @param <T>
 *            the type parameter
 * @author Kohsuke Kawaguchi
 */
public class PagedIterator<T> implements Iterator<T> {

    @Nonnull
    protected final Iterator<T[]> base;

    @CheckForNull
    private final Consumer<T> itemInitializer;

    /**
     * Current batch of items. Each time {@link #next()} is called the next item in this array will be returned. After
     * the last item of the array is returned, when {@link #next()} is called again, a new page of items will be fetched
     * and iterating will continue from the first item in the new page.
     *
     * @see #fetch() {@link #fetch()} for details on how this field is used.
     */
    private T[] currentPage;

    /**
     * The index of the next item on the page, the item that will be returned when {@link #next()} is called.
     *
     * @see #fetch() {@link #fetch()} for details on how this field is used.
     */
    private int nextItemIndex;

    PagedIterator(@Nonnull Iterator<T[]> base, @CheckForNull Consumer<T> itemInitializer) {
        this.base = base;
        this.itemInitializer = itemInitializer;
    }

    /**
     * This poorly named method, initializes items with local data after they are fetched. It is up to the implementer
     * to decide what local data to apply.
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
     * {@inheritDoc}
     */
    public boolean hasNext() {
        fetch();
        //https://github.com/hub4j/github-api/issues/1503
        return (currentPage != null && currentPage.length > nextItemIndex);
    }

    /**
     * {@inheritDoc}
     */
    public T next() {
        if (!hasNext())
            throw new NoSuchElementException();
        return currentPage[nextItemIndex++];
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
    private void fetch() {
        if ((currentPage == null || currentPage.length <= nextItemIndex) && base.hasNext()) {
            // On first call, always get next page (may be empty array)
            T[] result = Objects.requireNonNull(base.next());
            wrapUp(result);
            currentPage = result;
            nextItemIndex = 0;
        }
    }

    /**
     * Gets the next page worth of data.
     *
     * @return the list
     */
    public List<T> nextPage() {
        return Arrays.asList(nextPageArray());
    }

    /**
     * Gets the next page worth of data.
     *
     * @return the list
     */
    @Nonnull
    T[] nextPageArray() {
        // if we have not fetched any pages yet, always fetch.
        // If we have fetched at least one page, check hasNext()
        if (currentPage == null) {
            fetch();
        } else if (!hasNext()) {
            throw new NoSuchElementException();
        }

        // Current should never be null after fetch
        Objects.requireNonNull(currentPage);
        T[] r = currentPage;
        if (nextItemIndex != 0) {
            r = Arrays.copyOfRange(r, nextItemIndex, r.length);
        }
        nextItemIndex = currentPage.length;
        return r;
    }
}
