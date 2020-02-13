package org.kohsuke.github;

import java.io.IOException;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

/**
 * {@link PagedIterable} implementation that take a {@link Consumer} that initializes all the items on each page as they
 * are retrieved.
 *
 * @param <T>
 *            the type of items on each page
 */
class GitHubPageContentsIterable<T> extends PagedIterable<T> {

    private final GitHubClient client;
    private final GitHubRequest request;
    private final Class<T[]> clazz;
    private final Consumer<T> itemInitializer;

    GitHubPageContentsIterable(GitHubClient client,
            GitHubRequest request,
            Class<T[]> clazz,
            Consumer<T> itemInitializer) {
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
        return new GitHubPagedIterator(iterator);
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
        GitHubPagedIterator iterator = (GitHubPagedIterator) iterator();
        T[] items = toArray(iterator);
        GitHubResponse<T[]> lastResponse = iterator.lastResponse();
        return new GitHubResponse<>(lastResponse, items);
    }

    private class GitHubPagedIterator extends PagedIterator<T> {
        private final GitHubPageIterator<T[]> baseIterator;

        public GitHubPagedIterator(GitHubPageIterator<T[]> iterator) {
            super(iterator);
            baseIterator = iterator;
        }

        @Override
        protected void wrapUp(T[] page) {
            if (itemInitializer != null) {
                for (T item : page) {
                    itemInitializer.accept(item);
                }
            }
        }

        /**
         * Gets the {@link GitHubResponse} for the last page received.
         *
         * @return the {@link GitHubResponse} for the last page received.
         */
        private GitHubResponse<T[]> lastResponse() {
            return baseIterator.lastResponse();
        }
    }
}
