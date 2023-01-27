package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Iterator;

import javax.annotation.Nonnull;

// TODO: Auto-generated Javadoc
/**
 * {@link PagedIterable} enhanced to report search result specific information.
 *
 * @author Kohsuke Kawaguchi
 * @param <T>
 *            the type parameter
 */
@SuppressFBWarnings(
        value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD",
                "UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR" },
        justification = "Constructed by JSON API")
public class PagedSearchIterable<T> extends PagedIterable<T> {
    private final transient GitHub root;

    private final GitHubRequest request;

    private final Class<? extends SearchResult<T>> receiverType;

    /**
     * As soon as we have any result fetched, it's set here so that we can report the total count.
     */
    private SearchResult<T> result;

    /**
     * Instantiates a new paged search iterable.
     *
     * @param root
     *            the root
     * @param request
     *            the request
     * @param receiverType
     *            the receiver type
     */
    PagedSearchIterable(GitHub root, GitHubRequest request, Class<? extends SearchResult<T>> receiverType) {
        this.root = root;
        this.request = request;
        this.receiverType = receiverType;
    }

    /**
     * With page size.
     *
     * @param size
     *            the size
     * @return the paged search iterable
     */
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

    /**
     * Iterator.
     *
     * @param pageSize
     *            the page size
     * @return the paged iterator
     */
    @Nonnull
    @Override
    public PagedIterator<T> _iterator(int pageSize) {
        final Iterator<T[]> adapter = adapt(
                GitHubPageIterator.create(root.getClient(), receiverType, request, pageSize));
        return new PagedIterator<T>(adapter, null);
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
        };
    }
}
