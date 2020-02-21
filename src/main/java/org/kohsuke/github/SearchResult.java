package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Represents the result of a search
 */
abstract class SearchResult<T> {
    @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Field comes from JSON deserialization")
    int total_count;

    @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Field comes from JSON deserialization")
    boolean incomplete_results;

    /**
     * Wraps up the retrieved object and return them. Only called once.
     */
    abstract T[] getItems(GitHub root);
}
