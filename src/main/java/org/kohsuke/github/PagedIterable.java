package org.kohsuke.github;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * {@link Iterable} that returns {@link PagedIterator}
 *
 * @param <T>
 *            the type parameter
 * @author Kohsuke Kawaguchi
 */
public abstract class PagedIterable<T> implements Iterable<T> {
    /**
     * Page size. 0 is default.
     */
    private int size = 0;

    /**
     * Sets the pagination size.
     *
     * <p>
     * When set to non-zero, each API call will retrieve this many entries.
     *
     * @param size
     *            the size
     * @return the paged iterable
     */
    public PagedIterable<T> withPageSize(int size) {
        this.size = size;
        return this;
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @Nonnull
    public final PagedIterator<T> iterator() {
        return _iterator(size);
    }

    /**
     * Iterator paged iterator.
     *
     * @param pageSize
     *            the page size
     * @return the paged iterator
     */
    @Nonnull
    public abstract PagedIterator<T> _iterator(int pageSize);

    /**
     * Eagerly walk {@link Iterable} and return the result in a list.
     *
     * @return the list
     */
    public List<T> asList() {
        ArrayList<T> r = new ArrayList<>();
        for (PagedIterator<T> i = iterator(); i.hasNext();) {
            r.addAll(i.nextPage());
        }
        return r;
    }

    /**
     * Eagerly walk {@link Iterable} and return the result in a set.
     *
     * @return the set
     */
    public Set<T> asSet() {
        LinkedHashSet<T> r = new LinkedHashSet<>();
        for (PagedIterator<T> i = iterator(); i.hasNext();) {
            r.addAll(i.nextPage());
        }
        return r;
    }
}
