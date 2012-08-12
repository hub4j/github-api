package org.kohsuke.github;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Iterator over a pagenated data source.
 *
 * Aside from the normal iterator operation, this method exposes {@link #nextPage()}
 * that allows the caller to retrieve items per page and {@link #asList()}
 * that allows the caller to retrieve all items at once.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class PagedIterator<T> implements Iterator<T> {
    private final Iterator<T[]> base;

    /**
     * Current batch that we retrieved but haven't returned to the caller.
     */
    private T[] current;
    private int pos;

    /*package*/ PagedIterator(Iterator<T[]> base) {
        this.base = base;
    }

    protected abstract void wrapUp(T[] page);

    public boolean hasNext() {
        return (current!=null && pos<current.length) || base.hasNext();
    }

    public T next() {
        fetch();

        return current[pos++];
    }

    private void fetch() {
        while (current==null || current.length<=pos) {
            current = base.next();
            wrapUp(current);
            pos = 0;
        }
        // invariant at the end: there's some data to retrieve
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the next page worth of data.
     */
    public List<T> nextPage() {
        fetch();
        List<T> r = Arrays.asList(current);
        r = r.subList(pos,r.size());
        current = null;
        pos = 0;
        return r;
    }

    /**
     * Gets a list of all items
     */
    public List<T> asList() {
        List<T> r = new ArrayList<T>();
        for(Iterator i = this; i.hasNext();) {
            r.addAll(nextPage());
        }
        return r;
    }
}
