package org.kohsuke.github;

/**
 * Used to specify filters, sort order, etc for listing items in a collection.
 *
 * @param <T>
 *            the type parameter
 * @author Kohsuke Kawaguchi
 */
public abstract class GHQueryBuilder<T> extends GitHubInteractiveObject {
    protected final Requester req;

    GHQueryBuilder(GitHub root) {
        this.root = root;
        this.req = root.createRequest();
    }

    /**
     * Start listing items by using the settings built up on this object.
     *
     * @return the paged iterable
     */
    public abstract PagedIterable<T> list();
}
