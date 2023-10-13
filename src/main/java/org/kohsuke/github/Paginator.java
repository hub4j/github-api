package org.kohsuke.github;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public class Paginator<T> {
    /**
     * The base.
     */
    @Nonnull
    protected final NavigableIterator<T[]> base;

    @CheckForNull
    private final Consumer<T> itemInitializer;

    private T[] currentPage;
    private T[] nextPage;
    private T[] previousPage;

    private int nextItemIndex;

    Paginator(@Nonnull NavigableIterator<T[]> base, @CheckForNull Consumer<T> itemInitializer) {
        this.base = base;
        this.itemInitializer = itemInitializer;
    }

    protected void wrapUp(T[] page) {
        if (itemInitializer != null) {
            for (T item : page) {
                itemInitializer.accept(item);
            }
        }
    }

    public boolean hasPrevious() {
        return (currentPage != null && nextItemIndex > 0) || previousPage != null || base.hasPrevious();
    }

    public T previous() {
        if (!hasPrevious())
            throw new NoSuchElementException();
        if (currentPage != null && nextItemIndex > 0)
            return currentPage[--nextItemIndex];
        if (previousPage == null) {
            fetchPrevious();
        }
        if (previousPage == null) {
            throw new NoSuchElementException();
        }
        nextItemIndex = previousPage.length - 1;
        nextPage = currentPage;
        currentPage = previousPage;
        previousPage = null;
        return currentPage[nextItemIndex];
    }

    public T first() {
        nextItemIndex = 0;
        nextPage = null;
        currentPage = base.first();
        previousPage = null;
        return currentPage[nextItemIndex++];
    }

    T[] firstPageArray() {
        nextPage = null;
        previousPage = null;
        currentPage = base.first();
        nextItemIndex = currentPage.length;
        return currentPage;
    }

    public List<T> firstPageList() {
        return Arrays.asList(firstPageArray());
    }

    public T last() {
        nextPage = null;
        currentPage = base.last();
        nextItemIndex = currentPage.length - 1;
        previousPage = null;
        return currentPage[nextItemIndex++];
    }

    T[] lastPageArray() {
        nextPage = null;
        previousPage = null;
        currentPage = base.last();
        nextItemIndex = currentPage.length;
        return currentPage;
    }

    public List<T> lastPageList() {
        return Arrays.asList(lastPageArray());
    }

    public boolean hasNext() {
        return (currentPage != null && nextItemIndex < currentPage.length) || nextPage != null || base.hasNext();
    }

    public T next() {
        if (!hasNext())
            throw new NoSuchElementException();
        if (currentPage != null && nextItemIndex < currentPage.length)
            return currentPage[nextItemIndex++];
        if (nextPage == null) {
            fetchNext();
        }
        if (nextPage == null) {
            throw new NoSuchElementException();
        }
        nextItemIndex = 0;
        previousPage = currentPage;
        currentPage = nextPage;
        nextPage = null;
        return currentPage[nextItemIndex++];
    }

    public int totalPages() {
        return base.totalCount();
    }
    public int currentPage() {
        return base.currentPage();
    }

    private void fetchNext() {
        if (nextPage != null) {
            return;
        }
        if (base.hasNext()) {
            T[] result = Objects.requireNonNull(base.next());
            wrapUp(result);
            nextPage = result;
        }
    }

    private void fetchPrevious() {
        if (previousPage != null) {
            return;
        }
        if (base.hasPrevious()) {
            T[] result = Objects.requireNonNull(base.previous());
            wrapUp(result);
            previousPage = result;
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
     * Gets the next page worth of data.
     *
     * @return the list
     */
    public List<T> previousPage() {
        return Arrays.asList(previousPageArray());
    }

    /**
     * Gets the previous page worth of data.
     *
     * @return the list
     */
    @Nonnull
    T[] previousPageArray() {
        // if we have not fetched any pages yet, always fetch.
        // If we have fetched at least one page, check hasNext()
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
}
