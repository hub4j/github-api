package org.kohsuke.github;

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
    public abstract PagedIterator<T> iterator();

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
