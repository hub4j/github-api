package org.kohsuke.github;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * {@link Iterable} that returns {@link PagedIterator}
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class PagedIterable<T> implements Iterable<T> {
    /**
     * Page size. 0 is default.
     */
    private int size = 0;

    /**
     * Sets the pagination size.
     *
     * <p>
     * When set to non-zero, each API call will retrieve this many entries.
     */
    void setPageSize(int size) throws IOException {
        this.size = size;
    }

    public final PagedIterator<T> iterator() {
        return _iterator(size);
    }

    public abstract PagedIterator<T> _iterator(int pageSize);

    /**
     * Eagerly walk {@link Iterable} and return the result in a list.
     */
    public List<T> asList() {
        List<T> r = new ArrayList<T>();
        for(PagedIterator<T> i = iterator(); i.hasNext();) {
            r.addAll(i.nextPage());
        }
        return r;
    }

    /**
     * Eagerly walk {@link Iterable} and return the result in a set.
     */
    public Set<T> asSet() {
        LinkedHashSet<T> r = new LinkedHashSet<T>();
        for(PagedIterator<T> i = iterator(); i.hasNext();) {
            r.addAll(i.nextPage());
        }
        return r;
    }
}
