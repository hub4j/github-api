package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

// TODO: Auto-generated Javadoc
/**
 * {@link PagedIterable} enhanced to report search result specific information.
 *
 * @author Kohsuke Kawaguchi
 * @param <T>
 *            the type parameter
 */
@SuppressFBWarnings(
        value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD",
                "UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR" },
        justification = "Constructed by JSON API")
public class PagedSearchIterable<T> extends PagedIterable<T> {

    private final GitHubEndpointIterable<? extends SearchResult<T>, T> paginatedEndpoint;

    /**
     * Instantiates a new git hub page contents iterable.
     */
    <Result extends SearchResult<T>> PagedSearchIterable(GitHubEndpointIterable<Result, T> paginatedEndpoint) {
        super(paginatedEndpoint);
        this.paginatedEndpoint = paginatedEndpoint;
    }

    /**
     * Returns the total number of hit, including the results that's not yet fetched.
     *
     * @return the total count
     */
    public int getTotalCount() {
        // populate();
        return paginatedEndpoint.pageIterator().peek().totalCount;
    }

    /**
     * Is incomplete boolean.
     *
     * @return the boolean
     */
    public boolean isIncomplete() {
        // populate();
        return paginatedEndpoint.pageIterator().peek().incompleteResults;
    }
}
