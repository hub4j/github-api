package org.kohsuke.github;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * Iterator over a paginated data source. Iterates of the content items of each page, automatically requesting new pages
 * as needed.
 * <p>
 * Aside from the normal iterator operation, this method exposes {@link #nextPage()} and {@link #nextPageArray()} that
 * allows the caller to retrieve entire pages.
 *
 * @param <T>
 *            the type parameter
 * @author Kohsuke Kawaguchi
 */
public abstract class PagedIterator<T> implements Iterator<T> {
    protected final Iterator<T[]> base;

    /**
     * Current batch that we retrieved but haven't returned to the caller.
     */
    private T[] current;
    private int pos;

    PagedIterator(Iterator<T[]> base) {
        this.base = base;
    }

    /**
     * Wrap up.
     *
     * @param page
     *            the page
     */
    protected abstract void wrapUp(T[] page);

    public boolean hasNext() {
        fetch();
        return current.length > pos;
    }

    public T next() {
        if (!hasNext())
            throw new NoSuchElementException();
        return current[pos++];
    }

    private void fetch() {
        if ((current == null || current.length <= pos) && base.hasNext()) {
            // On first call, always get next page (may be empty array)
            T[] result = Objects.requireNonNull(base.next());
            wrapUp(result);
            current = result;
            pos = 0;
        }
    }

    public void remove() {
        throw new UnsupportedOperationException();
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
        if (current == null) {
            fetch();
        } else if (!hasNext()) {
            throw new NoSuchElementException();
        }

        // Current should never be null after fetch
        Objects.requireNonNull(current);
        T[] r = current;
        if (pos != 0) {
            r = Arrays.copyOfRange(r, pos, r.length);
        }
        pos = current.length;
        return r;
    }
}
