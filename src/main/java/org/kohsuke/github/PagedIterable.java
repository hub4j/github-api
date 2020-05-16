package org.kohsuke.github;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * {@link Iterable} that returns {@link PagedIterator}. {@link PagedIterable} is thread-safe but {@link PagedIterator}
 * is not. Any one instance of {@link PagedIterator} should only be called from a single thread.
 *
 * @param <T>
 *            the type of items on each page
 * @author Kohsuke Kawaguchi
 */
public abstract class PagedIterable<T> implements Iterable<T> {
    /**
     * Page size. 0 is default.
     */
    private int pageSize = 0;

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
        this.pageSize = size;
        return this;
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @Nonnull
    public final PagedIterator<T> iterator() {
        return _iterator(pageSize);
    }

    /**
     * Iterator over page items.
     *
     * @param pageSize
     *            the page size
     * @return the paged iterator
     */
    @Nonnull
    public abstract PagedIterator<T> _iterator(int pageSize);

    /**
     * Eagerly walk {@link PagedIterator} and return the result in an array.
     *
     * @param iterator
     *            the {@link PagedIterator} to read
     * @return an array of all elements from the {@link PagedIterator}
     * @throws IOException
     *             if an I/O exception occurs.
     */
    protected T[] toArray(final PagedIterator<T> iterator) throws IOException {
        try {
            ArrayList<T[]> pages = new ArrayList<>();
            int totalSize = 0;
            T[] item;
            do {
                item = iterator.nextPageArray();
                totalSize += Array.getLength(item);
                pages.add(item);
            } while (iterator.hasNext());

            Class<T[]> type = (Class<T[]>) item.getClass();

            return concatenatePages(type, pages, totalSize);
        } catch (GHException e) {
            // if there was an exception inside the iterator it is wrapped as a GHException
            // if the wrapped exception is an IOException, throw that
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw e;
            }
        }
    }

    /**
     * Eagerly walk {@link Iterable} and return the result in an array.
     *
     * @return the list
     * @throws IOException
     *             if an I/O exception occurs.
     */
    @Nonnull
    public T[] toArray() throws IOException {
        return toArray(iterator());
    }

    /**
     * Eagerly walk {@link Iterable} and return the result in a list.
     *
     * @return the list
     * @throws IOException
     *             if an I/O Exception occurs
     */
    @Nonnull
    public List<T> toList() throws IOException {
        return Collections.unmodifiableList(Arrays.asList(this.toArray()));
    }

    /**
     * Eagerly walk {@link Iterable} and return the result in a set.
     *
     * @return the set
     * @throws IOException
     *             if an I/O Exception occurs
     */
    @Nonnull
    public Set<T> toSet() throws IOException {
        return Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(this.toArray())));
    }

    /**
     * Eagerly walk {@link Iterable} and return the result in a list.
     *
     * @return the list
     * @deprecated Use {@link #toList()} instead.
     */
    @Nonnull
    @Deprecated
    public List<T> asList() {
        try {
            return this.toList();
        } catch (IOException e) {
            throw new GHException("Failed to retrieve list: " + e.getMessage(), e);
        }
    }

    /**
     * Eagerly walk {@link Iterable} and return the result in a set.
     *
     * @return the set
     * @deprecated Use {@link #toSet()} instead.
     */
    @Nonnull
    @Deprecated
    public Set<T> asSet() {
        try {
            return this.toSet();
        } catch (IOException e) {
            throw new GHException("Failed to retrieve list: " + e.getMessage(), e);
        }
    }

    /**
     * Concatenates a list of arrays into a single array.
     * 
     * @param type
     *            the type of array to be returned.
     * @param pages
     *            the list of arrays to be concatenated.
     * @param totalLength
     *            the total length of the returned array.
     * @return an array containing all elements from all pages.
     */
    @Nonnull
    private T[] concatenatePages(Class<T[]> type, List<T[]> pages, int totalLength) {

        T[] result = type.cast(Array.newInstance(type.getComponentType(), totalLength));

        int position = 0;
        for (T[] page : pages) {
            final int pageLength = Array.getLength(page);
            System.arraycopy(page, 0, result, position, pageLength);
            position += pageLength;
        }
        return result;
    }

}
