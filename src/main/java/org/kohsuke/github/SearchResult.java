package org.kohsuke.github;

/**
 * Represents the result of a search
 *
 * @author Kohsuke Kawaguchi
 */
abstract class SearchResult<T> {
    int total_count;
    boolean incomplete_results;

    public abstract T[] getItems();
}
