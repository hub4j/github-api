package org.kohsuke.github;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * {@link Iterable} that returns {@link PagedIterator}. {@link PagedIterable} is thread-safe but {@link PagedIterator}
 * is not. Any one instance of {@link PagedIterator} should only be called from a single thread.
 *
 * @author Kohsuke Kawaguchi
 * @param <T>
 *            the type of items on each page
 */
public class PagedIterable<T> implements Iterable<T> {

    private final PaginatedEndpoint<?, T> paginatedEndpoint;

    /**
     * Instantiates a new git hub page contents iterable.
     */
    PagedIterable(PaginatedEndpoint<?, T> paginatedEndpoint) {
        this.paginatedEndpoint = paginatedEndpoint;
    }

    @Nonnull
    public final Iterator<T> iterator() {
        return paginatedEndpoint.iterator();
    }

    @Nonnull
    public T[] toArray() throws IOException {
        return paginatedEndpoint.toArray();
    }

    @Nonnull
    public List<T> toList() throws IOException {
        return paginatedEndpoint.toList();
    }

    @Nonnull
    public Set<T> toSet() throws IOException {
        return paginatedEndpoint.toSet();
    }

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
        paginatedEndpoint.withPageSize(size);
        return this;
    }

    GitHubResponse<T[]> toResponse() throws IOException {
        return paginatedEndpoint.toResponse();
    }
}
