package org.kohsuke.github;

import java.util.ArrayList;
import java.util.List;

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
}
