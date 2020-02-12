package org.kohsuke.github;

import java.net.MalformedURLException;
import java.util.Iterator;

/**
 * May be used for any item that has pagination information.
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
     * @param type
     *            type of each page (not the items in the page).
     * @param <T>
     *            type of each page (not the items in the page).
     * @return
     */
    static <T> GitHubPageIterator<T> create(GitHubClient client, Class<T> type, GitHubRequest.Builder<?> builder) {
        try {
            return new GitHubPageIterator<>(client, type, builder.build());
        } catch (MalformedURLException e) {
            throw new GHException("Unable to build github Api URL", e);
        }
    }

    public boolean hasNext() {
        return delegate.hasNext();
    }

    public T next() {
        lastResponse = nextResponse();
        assert lastResponse.body() != null;
        return lastResponse.body();
    }

    public GitHubResponse<T> nextResponse() {
        return delegate.next();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public GitHubResponse<T> lastResponse() {
        return lastResponse;
    }
}
