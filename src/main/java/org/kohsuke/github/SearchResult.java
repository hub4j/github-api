package org.kohsuke.github;

/**
 * Represents the result of a search
 *
 * @author Kohsuke Kawaguchi
 */
abstract class SearchResult<T> {
    int total_count;
    boolean incomplete_results;

    /**
     * Wraps up the retrieved object and return them. Only called once.
     */
    /*package*/ abstract T[] getItems(GitHub root);
}
