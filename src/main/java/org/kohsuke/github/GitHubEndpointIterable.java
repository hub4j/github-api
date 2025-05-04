package org.kohsuke.github;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

/**
 * {@link GitHubEndpointIterable} implementation that take a {@link Consumer} that initializes all the items on each
 * page as they are retrieved.
 *
 * {@link GitHubEndpointIterable} is immutable and thread-safe, but the iterator returned from {@link #iterator()} is
 * not. Any one instance of iterator should only be called from a single thread.
 *
 * @author Liam Newman
 * @param <Item>
 *            the type of items on each page
 */
class GitHubEndpointIterable<Page extends GitHubPage<Item>, Item> implements Iterable<Item> {

    private static class ArrayIterable<I> extends GitHubEndpointIterable<GitHubPage<I>, I> {

        private class ArrayIterator extends GitHubEndpointPageIterator<GitHubPage<I>, I> {

            ArrayIterator(GitHubClient client,
                    Class<GitHubPage<I>> pageType,
                    GitHubRequest request,
                    int pageSize,
                    Consumer<I> itemInitializer) {
                super(client, pageType, request, pageSize, itemInitializer);
            }

            @Override
            @NotNull protected GitHubResponse<GitHubPage<I>> sendNextRequest() throws IOException {
                GitHubResponse<I[]> response = client.sendRequest(nextRequest,
                        (connectorResponse) -> GitHubResponse.parseBody(connectorResponse, receiverType));
                return new GitHubResponse<>(response, new GitHubArrayPage<>(response.body()));
            }

        }

        private final Class<I[]> receiverType;

        private ArrayIterable(GitHubClient client,
                GitHubRequest request,
                Class<I[]> receiverType,
                Consumer<I> itemInitializer) {
            super(client,
                    request,
                    GitHubArrayPage.getArrayPageClass(receiverType),
                    (Class<I>) receiverType.getComponentType(),
                    itemInitializer);
            this.receiverType = receiverType;
        }

        @NotNull @Override
        public GitHubEndpointPageIterator<GitHubPage<I>, I> pageIterator() {
            return new ArrayIterator(client, pageType, request, pageSize, itemInitializer);
        }
    }

    /**
     * Represents the result of a search.
     *
     * @author Kohsuke Kawaguchi
     * @param <I>
     *            the generic type
     */
    private static class GitHubArrayPage<I> implements GitHubPage<I> {

        private static <P extends GitHubPage<I>, I> Class<P> getArrayPageClass(Class<I[]> receiverType) {
            return (Class<P>) new GitHubArrayPage<>(receiverType).getClass();
        }

        private final I[] items;

        public GitHubArrayPage(I[] items) {
            this.items = items;
        }

        private GitHubArrayPage(Class<I[]> receiverType) {
            this.items = (I[]) Array.newInstance(receiverType.getComponentType(), 0);
        }

        public I[] getItems() {
            return items;
        }
    }

    static <I> GitHubEndpointIterable<GitHubPage<I>, I> ofArrayEndpoint(GitHubClient client,
            GitHubRequest request,
            Class<I[]> receiverType,
            Consumer<I> itemInitializer) {
        return new ArrayIterable<>(client, request, receiverType, itemInitializer);
    }

    static <I> GitHubEndpointIterable<GitHubPage<I>, I> ofSingleton(I[] array) {
        return ofSingleton(new GitHubArrayPage<>(array));
    }

    static <P extends GitHubPage<I>, I> GitHubEndpointIterable<P, I> ofSingleton(P page) {
        Class<I> itemType = (Class<I>) page.getItems().getClass().getComponentType();
        return new GitHubEndpointIterable<>(null, null, (Class<P>) page.getClass(), itemType, null) {
            @Nonnull
            @Override
            public GitHubEndpointPageIterator<P, I> pageIterator() {
                return GitHubEndpointPageIterator.ofSingleton(page);
            }
        };
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
    GitHubEndpointIterable(GitHubClient client,
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
    public final GitHubPageItemIterator<Page, Item> itemIterator() {
        return new GitHubPageItemIterator<>(this.pageIterator());
    }
    @Nonnull
    @Override
    public final Iterator<Item> iterator() {
        return this.itemIterator();
    }

    /**
     *
     * @return
     */
    @Nonnull
    public GitHubEndpointPageIterator<Page, Item> pageIterator() {
        return new GitHubEndpointPageIterator<>(client, pageType, request, pageSize, itemInitializer);
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
        return toArray(pageIterator(), itemType);
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
    public final Set<Item> toSet() throws IOException {
        return Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(this.toArray())));
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
    public final GitHubEndpointIterable<Page, Item> withPageSize(int size) {
        this.pageSize = size;
        return this;
    }

    /**
     * Concatenates a list of arrays into a single array.
     *
     * @param pages
     *            the list of arrays to be concatenated.
     * @param totalLength
     *            the total length of the returned array.
     * @return an array containing all elements from all pages.
     */
    @Nonnull
    private Item[] concatenatePages(List<Item[]> pages, int totalLength) {
        Item[] result = (Item[]) Array.newInstance(itemType, totalLength);

        int position = 0;
        for (Item[] page : pages) {
            final int pageLength = Array.getLength(page);
            System.arraycopy(page, 0, result, position, pageLength);
            position += pageLength;
        }
        return result;
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
    private Item[] toArray(final GitHubEndpointPageIterator<Page, Item> iterator, Class<Item> itemType) throws IOException {
        try {
            ArrayList<Item[]> pages = new ArrayList<>();
            int totalSize = 0;
            Item[] item;
            while (iterator.hasNext()) {
                item = iterator.next().getItems();
                totalSize += Array.getLength(item);
                pages.add(item);
            }

            return concatenatePages(pages, totalSize);
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
        GitHubEndpointPageIterator<Page, Item> iterator = pageIterator();
        Item[] items = toArray(iterator, itemType);
        GitHubResponse<Page> lastResponse = iterator.finalResponse();
        return new GitHubResponse<>(lastResponse, items);
    }
}
