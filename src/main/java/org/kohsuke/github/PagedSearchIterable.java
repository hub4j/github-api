package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Iterator;

import javax.annotation.Nonnull;

/**
 * {@link PagedIterable} enhanced to report search result specific information.
 *
 * @param <T>
 *            the type parameter
 */
@SuppressFBWarnings(
        value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD",
                "UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR" },
        justification = "Constructed by JSON API")
public class PagedSearchIterable<T> extends PagedIterable<T> {
    @Nonnull
    private final GitHub root;

    @Nonnull
    private final GitHubRequest request;

    /**
     * Data transfer object that receives the result of search.
     */
    @Nonnull
    private final Class<? extends SearchResult<T>> receiverType;

    /**
     * As soon as we have any result fetched, it's set here so that we can report the total count.
     */
    private SearchResult<T> result;

    PagedSearchIterable(@Nonnull GitHub root,
            @Nonnull GitHubRequest request,
            @Nonnull Class<? extends SearchResult<T>> receiverType) {
        this.root = root;
        this.receiverType = receiverType;
        this.request = request;
    }

    @Override
    public PagedSearchIterable<T> withPageSize(int size) {
        return (PagedSearchIterable<T>) super.withPageSize(size);
    }

    /**
     * Returns the total number of hit, including the results that's not yet fetched.
     *
     * @return the total count
     */
    public int getTotalCount() {
        populate();
        return result.total_count;
    }

    /**
     * Is incomplete boolean.
     *
     * @return the boolean
     */
    public boolean isIncomplete() {
        populate();
        return result.incomplete_results;
    }

    private void populate() {
        if (result == null)
            iterator().hasNext();
    }

    @Nonnull
    @Override
    public PagedIterator<T> _iterator(int pageSize) {
        final Iterator<T[]> adapter = adapt(GitHubPageIterator
                .create(root.getClient(), receiverType, this.request.toBuilder().withPageSize(pageSize)));
        return new GitHubPageContentsIterator<>(adapter, null);

    }

    /**
     * Adapts {@link Iterator}.
     *
     * @param base
     *            the base
     * @return the iterator
     */
    protected Iterator<T[]> adapt(final Iterator<? extends SearchResult<T>> base) {
        return new Iterator<T[]>() {
            public boolean hasNext() {
                return base.hasNext();
            }

            public T[] next() {
                SearchResult<T> v = base.next();
                if (result == null)
                    result = v;
                return v.getItems(root);
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
