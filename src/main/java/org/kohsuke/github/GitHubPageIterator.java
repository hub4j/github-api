package org.kohsuke.github;

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

    GitHubPageIterator(GitHubClient client, Class<T> type, GitHubRequest request) {
        this(new GitHubPageResponseIterator<>(client, type, request));
    }

    GitHubPageIterator(Iterator<GitHubResponse<T>> delegate) {
        this.delegate = delegate;
    }

    public boolean hasNext() {
        return delegate.hasNext();
    }

    public T next() {
        lastResponse = delegate.next();
        assert lastResponse.body() != null;
        return lastResponse.body();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public GitHubResponse<T> lastResponse() {
        return lastResponse;
    }
}
