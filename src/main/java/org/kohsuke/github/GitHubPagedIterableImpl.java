package org.kohsuke.github;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

/**
 *
 * @param <T>
 */
class GitHubPagedIterableImpl<T> extends PagedIterable<T> {

    private final GitHubClient client;
    private final GitHubRequest request;
    private final Class<T[]> clazz;
    private final Consumer<T> consumer;

    GitHubPagedIterableImpl(GitHubClient client, GitHubRequest request, Class<T[]> clazz, Consumer<T> consumer) {
        this.client = client;
        this.request = request;
        this.clazz = clazz;
        this.consumer = consumer;
    }

    @NotNull
    @Override
    @Nonnull
    public PagedIterator<T> _iterator(int pageSize) {
        final Iterator<T[]> iterator = GitHubPageIterator
                .create(client, clazz, request.toBuilder().withPageSize(pageSize));
        return new PagedIterator<T>(iterator) {
            @Override
            protected void wrapUp(T[] page) {
                if (consumer != null) {
                    for (T item : page) {
                        consumer.accept(item);
                    }
                }
            }
        };
    }
}
