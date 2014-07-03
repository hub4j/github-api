package org.kohsuke.github;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

/**
 * Set of {@link GHPerson} with helper lookup methods.
 * 
 * @author Kohsuke Kawaguchi
 */
public class GHPersonSet<T extends GHPerson> extends HashSet<T> {
    public GHPersonSet() {
    }

    public GHPersonSet(Collection<? extends T> c) {
        super(c);
    }

    public GHPersonSet(T... c) {
        super(Arrays.asList(c));
    }

    public GHPersonSet(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public GHPersonSet(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Finds the item by its login.
     */
    public T byLogin(String login) {
        for (T t : this)
            if (t.getLogin().equals(login))
                return t;
        return null;
    }    
}
