package org.kohsuke.github;

// TODO: Auto-generated Javadoc
/**
 * Represents the result of a search.
 *
 * @author Kohsuke Kawaguchi
 * @param <T>
 *            the generic type
 */
abstract class SearchResult<T> {

    /** The total count. */
    int totalCount;

    /** The incomplete results. */
    boolean incompleteResults;

    /**
     * Wraps up the retrieved object and return them. Only called once.
     *
     * @param root
     *            the root
     * @return the items
     */
    abstract T[] getItems(GitHub root);
}
