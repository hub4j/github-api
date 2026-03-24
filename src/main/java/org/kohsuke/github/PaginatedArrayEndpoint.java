package org.kohsuke.github;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
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
class PaginatedArrayEndpoint<Item> extends PaginatedEndpoint<GitHubPage<Item>, Item> {

    private class ArrayPages extends PaginatedEndpointPages<GitHubPage<Item>, Item> {

        ArrayPages(GitHubClient client,
                Class<GitHubPage<Item>> pageType,
                GitHubRequest request,
                int pageSize,
                Consumer<Item> itemInitializer) {
            super(client, pageType, request, pageSize, itemInitializer);
        }

        @Override
        @NotNull protected GitHubResponse<GitHubPage<Item>> sendNextRequest() throws IOException {
            GitHubResponse<Item[]> response = client.sendRequest(nextRequest,
                    (connectorResponse) -> GitHubResponse.parseBody(connectorResponse, arrayReceiverType));
            return new GitHubResponse<>(response, new GitHubPageArrayAdapter<>(response.body()));
        }
    }

    /**
     * Pretend to get a specific page class type for the sake of the compile time strong typing.
     *
     * This class never uses {@code pageType}, so it is safe to pass null as the actual class value.
     *
     * @param <I>
     *            The type of items in the array page.
     * @param itemType
     *            The class instance for items in the array page.
     * @return Always null, but cast to the appropriate class for compile time strong typing.
     */
    private static <I> Class<GitHubPage<I>> getArrayPageClass(Class<I> itemType) {
        return (Class<GitHubPage<I>>) null;
    }

    private final Class<Item[]> arrayReceiverType;

    PaginatedArrayEndpoint(GitHubClient client,
            GitHubRequest request,
            Class<Item[]> arrayReceiverType,
            Class<Item> itemType,
            Consumer<Item> itemInitializer) {
        super(client, request, getArrayPageClass(itemType), itemType, itemInitializer);
        this.arrayReceiverType = arrayReceiverType;
    }

    @Nonnull
    @Override
    public PaginatedEndpointPages<GitHubPage<Item>, Item> pages() {
        return new ArrayPages(client, pageType, request, pageSize, itemInitializer);
    }
}
