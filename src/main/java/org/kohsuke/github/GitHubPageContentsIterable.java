package org.kohsuke.github;

import java.io.IOException;
import java.util.function.Consumer;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * {@link PagedIterable} implementation that take a {@link Consumer} that initializes all the items on each page as they
 * are retrieved.
 *
 * {@link GitHubPageContentsIterable} is immutable and thread-safe, but the iterator returned from {@link #iterator()}
 * is not. Any one instance of iterator should only be called from a single thread.
 *
 * @param <T>
 *            the type of items on each page
 */
class GitHubPageContentsIterable<T> extends PagedIterable<T> {

    @Nonnull
    private final GitHubClient client;

    @Nonnull
    private final GitHubRequest request;

    @Nonnull
    private final Class<T[]> clazz;

    @CheckForNull
    private final Consumer<T> itemInitializer;

    GitHubPageContentsIterable(@Nonnull GitHubClient client,
            @Nonnull GitHubRequest request,
            @Nonnull Class<T[]> clazz,
            @CheckForNull Consumer<T> itemInitializer) {
        this.client = client;
        this.request = request;
        this.clazz = clazz;
        this.itemInitializer = itemInitializer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public PagedIterator<T> _iterator(int pageSize) {
        final GitHubPageIterator<T[]> iterator = GitHubPageIterator
                .create(client, clazz, request.toBuilder().withPageSize(pageSize));
        return new GitHubPageContentsIterator<>(iterator, itemInitializer);
    }

    /**
     * Eagerly walk {@link Iterable} and return the result in a {@link GitHubResponse} containing an array of {@link T}
     * items.
     *
     * @return the last response with an array containing all the results from all pages.
     * @throws IOException
     *             if an I/O exception occurs.
     */
    @Nonnull
    GitHubResponse<T[]> toResponse() throws IOException {
        GitHubPageContentsIterator<T> iterator = (GitHubPageContentsIterator<T>) iterator();
        T[] items = toArray(iterator);
        GitHubResponse<T[]> lastResponse = iterator.lastResponse();
        return new GitHubResponse<>(lastResponse, items);
    }

}
