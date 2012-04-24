package org.kohsuke.github;

/**
 * @author Kohsuke Kawaguchi
 */
public interface PagedIterable<T> extends Iterable<T> {
    PagedIterator<T> iterator();
}
