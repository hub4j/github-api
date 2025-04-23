package org.kohsuke.github;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

// TODO: Auto-generated Javadoc
/**
 * {@link Iterable} that returns {@link PagedIterator}. {@link PagedIterable} is thread-safe but {@link PagedIterator}
 * is not. Any one instance of {@link PagedIterator} should only be called from a single thread.
 *
 * @author Kohsuke Kawaguchi
 * @param <T>
 *            the type of items on each page
 */
public class PagedIterable<T> implements Iterable<T> {

    private final GitHubEndpointIterable<?, T> paginatedEndpoint;

    /**
     * Instantiates a new git hub page contents iterable.
     */
    PagedIterable(GitHubEndpointIterable<?, T> paginatedEndpoint) {
        this.paginatedEndpoint = paginatedEndpoint;
    }

    public PagedIterator<T> _iterator(int pageSize) {
        throw new RuntimeException("No longer used.");
    }

    @Nonnull
    public final PagedIterator<T> iterator() {
        return new PagedIterator<>(paginatedEndpoint.itemIterator());
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

    public PagedIterable<T> withPageSize(int i) {
        paginatedEndpoint.withPageSize(i);
        return this;
    }

    GitHubResponse<T[]> toResponse() throws IOException {
        return paginatedEndpoint.toResponse();
    }
}
