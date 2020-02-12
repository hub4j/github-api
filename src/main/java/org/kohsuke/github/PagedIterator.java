package org.kohsuke.github;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * Iterator over a paginated data source.
 * <p>
 * Aside from the normal iterator operation, this method exposes {@link #nextPage()} that allows the caller to retrieve
 * items per page.
 *
 * @param <T>
 *            the type parameter
 * @author Kohsuke Kawaguchi
 */
public abstract class PagedIterator<T> implements Iterator<T> {
    private final Iterator<T[]> base;

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
            current = base.next();
            wrapUp(current);
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
        fetch();
        List<T> r = Arrays.asList(current);
        r = r.subList(pos, r.size());
        pos = current.length;
        return r;
    }

    /**
     * Gets the next page worth of data.
     *
     * @return the list
     */
    @Nonnull
    public T[] nextPageArray() {
        fetch();
        // Current should never be null after fetch
        Objects.requireNonNull(current);
        T[] r = current;
        if (pos != 0) {
            r = Arrays.copyOfRange(r, pos, r.length);
        }
        pos = current.length;
        return r;
    }

    /**
     * Gets the next page worth of data.
     *
     * @return the list
     */
    GitHubResponse<T[]> lastResponse() {
        if (!(base instanceof GitHubPageIterator)) {
            throw new IllegalStateException("Cannot get lastResponse for " + base.getClass().toString());
        }
        return ((GitHubPageIterator<T[]>) base).lastResponse();
    }

}
