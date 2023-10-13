package org.kohsuke.github;

import java.util.Iterator;
import java.util.NoSuchElementException;

public interface NavigableIterator<E> extends Iterator<E> {
    /**
     * Returns {@code true} if this list iterator has more elements when traversing the list in the reverse direction.
     * (In other words, returns {@code true} if {@link #previous} would return an element rather than throwing an
     * exception.)
     *
     * @return {@code true} if the list iterator has more elements when traversing the list in the reverse direction
     */
    boolean hasPrevious();

    /**
     * Returns the previous element in the list and moves the cursor position backwards. This method may be called
     * repeatedly to iterate through the list backwards, or intermixed with calls to {@link #next} to go back and forth.
     * (Note that alternating calls to {@code next} and {@code previous} will return the same element repeatedly.)
     *
     * @return the previous element in the list
     * @throws NoSuchElementException
     *             if the iteration has no previous element
     */
    E previous();

    E first();

    E last();

    int totalCount();

    int currentPage();
}
