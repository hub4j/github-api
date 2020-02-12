package org.kohsuke.github;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
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
     * Iterator paged iterator.
     *
     * @param pageSize
     *            the page size
     * @return the paged iterator
     */
    @Nonnull
    public abstract PagedIterator<T> _iterator(int pageSize);

    /**
     * Eagerly walk {@link Iterable} and return the result in a response containing an array.
     *
     * @return the list
     * @throws IOException
     */
    @Nonnull
    GitHubResponse<T[]> toResponse() throws IOException {
        GitHubResponse<T[]> result;

        try {
            ArrayList<T[]> pages = new ArrayList<>();
            PagedIterator<T> iterator = iterator();
            int totalSize = 0;
            T[] item;
            do {
                item = iterator.nextPageArray();
                totalSize += Array.getLength(item);
                pages.add(item);
            } while (iterator.hasNext());

            // At this point should always be at least one response and it should have a result
            // thought that might be an empty array.
            GitHubResponse<T[]> lastResponse = iterator.lastResponse();
            Class<T[]> type = (Class<T[]>) item.getClass();

            result = new GitHubResponse<>(lastResponse, concatenatePages(type, pages, totalSize));
        } catch (GHException e) {
            // if there was an exception inside the iterator it is wrapped as a GHException
            // if the wrapped exception is an IOException, throw that
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw e;
            }
        }
        return result;
    }

    /**
     * Eagerly walk {@link Iterable} and return the result in an array.
     *
     * @return the list
     */
    @Nonnull
    public T[] toArray() throws IOException {
        T[] result = toResponse().body();
        return result;
    }

    /**
     * Eagerly walk {@link Iterable} and return the result in a list.
     *
     * @return the list
     */
    @Nonnull
    public List<T> asList() {
        try {
            return Arrays.asList(this.toArray());
        } catch (IOException e) {
            throw new GHException("Failed to retrieve list: " + e.getMessage(), e);
        }
    }

    /**
     * Eagerly walk {@link Iterable} and return the result in a set.
     *
     * @return the set
     */
    @Nonnull
    public Set<T> asSet() {
        return new LinkedHashSet<>(this.asList());
    }

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
