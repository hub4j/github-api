package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

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
    @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Field comes from JSON deserialization")
    int total_count;

    /** The incomplete results. */
    @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Field comes from JSON deserialization")
    boolean incomplete_results;

    /**
     * Wraps up the retrieved object and return them. Only called once.
     *
     * @param root
     *            the root
     * @return the items
     */
    abstract T[] getItems(GitHub root);
}
