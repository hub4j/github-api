package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

/**
 * {@link PaginatedEndpoint} implementation that take a {@link Consumer} that initializes all the items on each page as
 * they are retrieved.
 *
 * {@link PaginatedEndpoint} is immutable and thread-safe, but the iterator returned from {@link #iterator()} is not.
 * Any one instance of iterator should only be called from a single thread.
 *
 * @author Liam Newman
 * @param <Item>
 *            the type of items on each page
 */
class PaginatedEndpoint<Page extends GitHubPage<Item>, Item> implements Iterable<Item> {

    private static class ArrayIterable<I> extends PaginatedEndpoint<GitHubArrayPage<I>, I> {

        private class ArrayIterator extends PaginatedEndpointPages<GitHubArrayPage<I>, I> {

            ArrayIterator(GitHubClient client,
                    Class<GitHubArrayPage<I>> pageType,
                    GitHubRequest request,
                    int pageSize,
                    Consumer<I> itemInitializer) {
                super(client, pageType, request, pageSize, itemInitializer);
            }

            @Override
            @NotNull protected GitHubResponse<GitHubArrayPage<I>> sendNextRequest() throws IOException {
                GitHubResponse<I[]> response = client.sendRequest(nextRequest,
                        (connectorResponse) -> GitHubResponse.parseBody(connectorResponse, receiverType));
                return new GitHubResponse<>(response, new GitHubArrayPage<>(response.body()));
            }

        }

        private final Class<I[]> receiverType;

        private ArrayIterable(GitHubClient client,
                GitHubRequest request,
                Class<I[]> receiverType,
                Class<I> itemType,
                Consumer<I> itemInitializer) {
            super(client, request, GitHubArrayPage.getArrayPageClass(itemType), itemType, itemInitializer);
            this.receiverType = receiverType;
        }

        @Nonnull
        @Override
        public PaginatedEndpointPages<GitHubArrayPage<I>, I> pages() {
            return new ArrayIterator(client, pageType, request, pageSize, itemInitializer);
        }
    }

    /**
     * Represents a page of results
     *
     * @author Kohsuke Kawaguchi
     * @param <I>
     *            the generic type
     */
    private static class GitHubArrayPage<I> implements GitHubPage<I> {

        @SuppressFBWarnings(value = { "DM_NEW_FOR_GETCLASS" }, justification = "BUG?")
        private static <P extends GitHubPage<I>, I> Class<P> getArrayPageClass(Class<I> itemType) {
            return (Class<P>) new GitHubArrayPage<>(itemType).getClass();
        }

        private final I[] items;

        public GitHubArrayPage(I[] items) {
            this.items = items;
        }

        private GitHubArrayPage(Class<I> itemType) {
            this.items = null;
        }

        public I[] getItems() {
            return items;
        }
    }

    private static class SinglePageEndpoint<P extends GitHubPage<I>, I> extends PaginatedEndpoint<P, I> {
        private final P page;

        SinglePageEndpoint(P page, Class<I> itemType) {
            super(null, null, (Class<P>) page.getClass(), itemType, null);
            this.page = page;
        }

        @Nonnull
        @Override
        public PaginatedEndpointPages<P, I> pages() {
            return PaginatedEndpointPages.fromSinglePage(pageType, page);
        }

    }

    static <I> PaginatedEndpoint<GitHubArrayPage<I>, I> fromSinglePage(I[] array, Class<I> itemType) {
        return fromSinglePage(new GitHubArrayPage<>(array), itemType);
    }

    static <P extends GitHubPage<I>, I> PaginatedEndpoint<P, I> fromSinglePage(P page, Class<I> itemType) {
        return new SinglePageEndpoint<>(page, itemType);
    }

    static <I> PaginatedEndpoint<GitHubArrayPage<I>, I> ofArrayEndpoint(GitHubClient client,
            GitHubRequest request,
            Class<I[]> receiverType,
            Consumer<I> itemInitializer) {
        return new ArrayIterable<I>(client,
                request,
                (Class<I[]>) receiverType,
                (Class<I>) receiverType.getComponentType(),
                itemInitializer);
    }

    protected final GitHubClient client;
    protected final Consumer<Item> itemInitializer;
    protected final Class<Item> itemType;

    /**
     * Page size. 0 is default.
     */
    protected int pageSize = 0;

    protected final Class<Page> pageType;

    protected final GitHubRequest request;

    /**
     * Instantiates a new git hub page contents iterable.
     *
     * @param client
     *            the client
     * @param request
     *            the request
     * @param pageType
     *            the receiver type
     * @param itemInitializer
     *            the item initializer
     */
    PaginatedEndpoint(GitHubClient client,
            GitHubRequest request,
            Class<Page> pageType,
            Class<Item> itemType,
            Consumer<Item> itemInitializer) {
        this.client = client;
        this.request = request;
        this.pageType = pageType;
        this.itemType = itemType;
        this.itemInitializer = itemInitializer;
    }

    @Nonnull
    public final PaginatedEndpointItems<Item> items() {
        return new PaginatedEndpointItems<>(this.pages());
    }

    @Nonnull
    @Override
    public final Iterator<Item> iterator() {
        return this.items();
    }

    /**
     *
     * @return
     */
    @Nonnull
    public PaginatedEndpointPages<Page, Item> pages() {
        return new PaginatedEndpointPages<>(client, pageType, request, pageSize, itemInitializer);
    }

    /**
     * Eagerly walk {@link Iterable} and return the result in an array.
     *
     * @return the list
     * @throws IOException
     *             if an I/O exception occurs.
     */
    @Nonnull
    public final Item[] toArray() throws IOException {
        return toList().toArray((Item[]) Array.newInstance(itemType, 0));
    }

    /**
     * Eagerly walk {@link Iterable} and return the result in a list.
     *
     * @return the list
     * @throws IOException
     *             if an I/O Exception occurs
     */
    @Nonnull
    public final List<Item> toList() throws IOException {
        return Collections.unmodifiableList(toList(pages(), itemType));
    }

    /**
     * Eagerly walk {@link Iterable} and return the result in a set.
     *
     * @return the set
     * @throws IOException
     *             if an I/O Exception occurs
     */
    @Nonnull
    public final Set<Item> toSet() throws IOException {
        return Collections.unmodifiableSet(new LinkedHashSet<>(toList()));
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
    public final PaginatedEndpoint<Page, Item> withPageSize(int size) {
        this.pageSize = size;
        return this;
    }

    /**
     * Eagerly walk {@link PagedIterator} and return the result in an array.
     *
     * @param iterator
     *            the {@link PagedIterator} to read
     * @return an array of all elements from the {@link PagedIterator}
     * @throws IOException
     *             if an I/O exception occurs.
     */
    private List<Item> toList(final PaginatedEndpointPages<Page, Item> iterator, Class<Item> itemType)
            throws IOException {
        try {
            ArrayList<Item> pageList = new ArrayList<>();
            iterator.forEachRemaining(page -> {
                pageList.addAll(Arrays.asList(page.getItems()));
            });
            return pageList;
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
     * Eagerly walk {@link Iterable} and return the result in a {@link GitHubResponse} containing an array of {@code T}
     * items.
     *
     * @return the last response with an array containing all the results from all pages.
     * @throws IOException
     *             if an I/O exception occurs.
     */
    @Nonnull
    final GitHubResponse<Item[]> toResponse() throws IOException {
        PaginatedEndpointPages<Page, Item> iterator = pages();
        Item[] items = toList(iterator, itemType).toArray((Item[]) Array.newInstance(itemType, 0));
        GitHubResponse<Page> lastResponse = iterator.finalResponse();
        return new GitHubResponse<>(lastResponse, items);
    }
}
