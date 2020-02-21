package org.kohsuke.github;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

/**
 * Set of {@link GHPerson} with helper lookup methods.
 *
 * @param <T>
 *            the type parameter
 */
public class GHPersonSet<T extends GHPerson> extends HashSet<T> {
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new Gh person set.
     */
    public GHPersonSet() {
    }

    /**
     * Instantiates a new Gh person set.
     *
     * @param c
     *            the c
     */
    public GHPersonSet(Collection<? extends T> c) {
        super(c);
    }

    /**
     * Instantiates a new Gh person set.
     *
     * @param c
     *            the c
     */
    public GHPersonSet(T... c) {
        super(Arrays.asList(c));
    }

    /**
     * Instantiates a new Gh person set.
     *
     * @param initialCapacity
     *            the initial capacity
     * @param loadFactor
     *            the load factor
     */
    public GHPersonSet(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    /**
     * Instantiates a new Gh person set.
     *
     * @param initialCapacity
     *            the initial capacity
     */
    public GHPersonSet(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Finds the item by its login.
     *
     * @param login
     *            the login
     * @return the t
     */
    public T byLogin(String login) {
        for (T t : this)
            if (t.getLogin().equals(login))
                return t;
        return null;
    }
}
