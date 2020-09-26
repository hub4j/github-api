package org.kohsuke.github;

import java.io.IOException;
import java.util.function.Consumer;

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

    private final GitHubClient client;
    private final GitHubRequest request;
    private final Class<T[]> receiverType;
    private final Consumer<T> itemInitializer;
    private final String nestedFieldKey;

    GitHubPageContentsIterable(GitHubClient client,
            GitHubRequest request,
            Class<T[]> receiverType,
            Consumer<T> itemInitializer,
            String nestedFieldKey) {
        this.client = client;
        this.request = request;
        this.receiverType = receiverType;
        this.itemInitializer = itemInitializer;
        this.nestedFieldKey = nestedFieldKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public PagedIterator<T> _iterator(int pageSize) {
        final GitHubPageIterator<T[]> iterator = GitHubPageIterator
                .create(client, receiverType, request, pageSize, nestedFieldKey);
        return new GitHubPageContentsIterator(iterator, itemInitializer);
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
        GitHubPageContentsIterator iterator = (GitHubPageContentsIterator) iterator();
        T[] items = toArray(iterator);
        GitHubResponse<T[]> lastResponse = iterator.lastResponse();
        return new GitHubResponse<>(lastResponse, items);
    }

    /**
     * This class is not thread-safe. Any one instance should only be called from a single thread.
     */
    private class GitHubPageContentsIterator extends PagedIterator<T> {

        public GitHubPageContentsIterator(GitHubPageIterator<T[]> iterator, Consumer<T> itemInitializer) {
            super(iterator, itemInitializer);
        }

        /**
         * Gets the {@link GitHubResponse} for the last page received.
         *
         * @return the {@link GitHubResponse} for the last page received.
         */
        private GitHubResponse<T[]> lastResponse() {
            return ((GitHubPageIterator<T[]>) base).finalResponse();
        }
    }
}
