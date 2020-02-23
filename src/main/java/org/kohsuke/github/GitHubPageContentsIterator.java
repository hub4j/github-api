package org.kohsuke.github;

import java.util.Iterator;
import java.util.function.Consumer;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This class is not thread-safe. Any one instance should only be called from a single thread.
 */
class GitHubPageContentsIterator<T> extends PagedIterator<T> {

    @CheckForNull
    private final Consumer<T> itemInitializer;

    public GitHubPageContentsIterator(@Nonnull Iterator<T[]> iterator, @CheckForNull Consumer<T> itemInitializer) {
        super(iterator);
        this.itemInitializer = itemInitializer;
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
    GitHubResponse<T[]> lastResponse() {
        return ((GitHubPageIterator<T[]>) base).finalResponse();
    }
}
