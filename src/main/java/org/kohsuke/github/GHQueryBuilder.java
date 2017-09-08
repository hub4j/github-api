package org.kohsuke.github;

/**
 * Used to specify filters, sort order, etc for listing items in a collection.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class GHQueryBuilder<T> {
    protected final GitHub root;
    protected final Requester req;

    /*package*/ GHQueryBuilder(GitHub root) {
        this.root = root;
        this.req = root.retrieve();
    }

    /**
     * Start listing items by using the settings built up on this object.
     */
    public abstract PagedIterable<T> list();
}
