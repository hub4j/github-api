package org.kohsuke.github;

/**
 * Represents a page of results
 *
 * @author Kohsuke Kawaguchi
 * @param <I>
 *            the generic type
 */
class GitHubPageArrayAdapter<I> implements GitHubPage<I> {

    private final I[] items;

    public GitHubPageArrayAdapter(I[] items) {
        this.items = items;
    }

    public I[] getItems() {
        return items;
    }
}
