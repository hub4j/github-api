package org.kohsuke.github;

import java.util.Arrays;
import java.util.List;

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

    default List<I> getItemsList() {
        return Arrays.asList(this.getItems());
    }
}
