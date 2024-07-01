package org.kohsuke.github;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Extension to {@link Iterator} supporting bidirectional iteration and also jumping to first or last entry.
 *
 * @author Anuj Hydrabadi
 * @param <E>
 *            the type of the page of the data.
 */
public interface NavigableIterator<E> extends Iterator<E> {
    /**
     * Returns {@code true} if this iterator has more elements when traversing the list in the reverse direction. (In
     * other words, returns {@code true} if {@link #previous} would return an element rather than throwing an
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

    /**
     * Returns the last element in the list and sets the cursor after the last element, such that a call to
     * {@link #hasNext()} returns false, and a call to {@link #previous()} returns the last element again.
     *
     * @return the last element in the list.
     */
    E last();

    /**
     * Returns the first element in the list and sets the cursor onto the second element, such that a call to
     * {@link #next()} returns the second element, and a call to {@link #previous()} returns the first element again.
     *
     * @return the first element in the list.
     */
    E first();
}
