package org.kohsuke.github;

import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * May be used for any item that has pagination information. Iterates over paginated {@link T} objects (not the items
 * inside the page). Also exposes {@link #nextResponse()} to allow getting the full {@link GitHubResponse<T>} instead of
 * T.
 *
 * Works for array responses, also works for search results which are single instances with an array of items inside.
 *
 * @param <T>
 *            type of each page (not the items in the page).
 */
class GitHubPageIterator<T> implements Iterator<T> {

    private final Iterator<GitHubResponse<T>> delegate;
    private GitHubResponse<T> lastResponse = null;

    public GitHubPageIterator(GitHubClient client, Class<T> type, GitHubRequest request) {
        this(new GitHubPageResponseIterator<>(client, type, request));
        if (!"GET".equals(request.method())) {
            throw new IllegalStateException("Request method \"GET\" is required for iterator.");
        }

    }

    GitHubPageIterator(Iterator<GitHubResponse<T>> delegate) {
        this.delegate = delegate;
    }

    /**
     * Loads paginated resources.
     *
     * @param client
     *            the {@link GitHubClient} from which to request responses
     * @param type
     *            type of each page (not the items in the page).
     * @param <T>
     *            type of each page (not the items in the page).
     * @return iterator
     */
    static <T> GitHubPageIterator<T> create(GitHubClient client, Class<T> type, GitHubRequest.Builder<?> builder) {
        try {
            return new GitHubPageIterator<>(client, type, builder.build());
        } catch (MalformedURLException e) {
            throw new GHException("Unable to build GitHub API URL", e);
        }
    }

    public boolean hasNext() {
        return delegate.hasNext();
    }

    /**
     * Gets the next page.
     * 
     * @return the next page.
     */
    @Nonnull
    public T next() {
        return Objects.requireNonNull(nextResponse().body());
    }

    /**
     * Gets the next response page.
     * 
     * @return the next response page.
     */
    @Nonnull
    public GitHubResponse<T> nextResponse() {
        GitHubResponse<T> result = Objects.requireNonNull(delegate.next());
        lastResponse = result;
        return result;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public GitHubResponse<T> lastResponse() {
        return lastResponse;
    }
}
