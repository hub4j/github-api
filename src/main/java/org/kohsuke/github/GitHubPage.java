package org.kohsuke.github;

/**
 * A page of results from GitHub.
 *
 * @param <I>
 *            the type of items on the page.
 */
interface GitHubPage<I> {
    /**
     * Wraps up the retrieved object and return them. Only called once.
     *
     * @return the items
     */
    I[] getItems();
}
