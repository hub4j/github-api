package org.kohsuke.github;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

/**
 * May be used for any item that has pagination information. Iterates over paginated {@code P} objects (not the items
 * inside the page). Also exposes {@link #finalResponse()} to allow getting a full {@link GitHubResponse} {@code
 *
<P>
 * } after iterating completes.
 *
 * Works for array responses, also works for search results which are single instances with an array of items inside.
 *
 * This class is not thread-safe. Any one instance should only be called from a single thread.
 *
 * @author Liam Newman
 * @param <P>
 *            type of each page (not the items in the page).
 */
class GitHubPageIterator<P extends GitHubPage<Item>, Item> implements Iterator<P> {

    static <P extends GitHubPage<Item>, Item> GitHubPageIterator<P, Item> ofSingleton(final P page) {
        return new GitHubPageIterator<>(page);
    }

    private final Consumer<Item> itemInitializer;

    /**
     * The page that will be returned when {@link #next()} is called.
     *
     * <p>
     * Will be {@code null} after {@link #next()} is called.
     * </p>
     * <p>
     * Will not be {@code null} after {@link #fetchNext()} is called if a new page was fetched.
     * </p>
     */
    protected P next;
    protected final Class<P> pageType;

    private GitHubPageIterator(P page) {
        this((Class<P>) page.getClass(), null);
        this.next = page;
    }

    protected GitHubPageIterator(Class<P> pageType, Consumer<Item> itemInitializer) {
        this.pageType = pageType;
        this.itemInitializer = itemInitializer;
    }

    /**
     * On rare occasions the final response from iterating is needed.
     *
     * @return the final response of the iterator.
     */
    public GitHubResponse<P> finalResponse() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasNext() {
        return peek() != null;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    public P next() {
        P result = peek();
        if (result == null)
            throw new NoSuchElementException();
        next = null;
        return result;
    }

    /**
     *
     * @return
     */
    public P peek() {
        if (next == null) {
            P result = fetchNext();
            if (result != null) {
                next = result;
                initializeItems();
            }
        }
        return next;
    }

    /**
     * This method initializes items with local data after they are fetched. It is up to the implementer to decide what
     * local data to apply.
     *
     */
    private void initializeItems() {
        if (itemInitializer != null) {
            for (Item item : next.getItems()) {
                itemInitializer.accept(item);
            }
        }
    }

    /**
     * This method is called at the start of {@link #hasNext()} or {@link #next()} to fetch another page of data if it
     * is needed.
     */
    protected P fetchNext() {
        return null;
    }
}
