package org.kohsuke.github;

// TODO: Auto-generated Javadoc
/**
 * Used to specify filters, sort order, etc for listing items in a collection.
 *
 * @author Kohsuke Kawaguchi
 * @param <T>            the type parameter
 */
public abstract class GHQueryBuilder<T> extends GitHubInteractiveObject {
    
    /** The req. */
    protected final Requester req;

    /**
     * Instantiates a new GH query builder.
     *
     * @param root the root
     */
    GHQueryBuilder(GitHub root) {
        super(root);
        this.req = root.createRequest();
    }

    /**
     * Start listing items by using the settings built up on this object.
     *
     * @return the paged iterable
     */
    public abstract PagedIterable<T> list();
}
