/*
 * GitHub API for Java
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.kohsuke.github;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Iterator over a paginated data source.
 *
 * Aside from the normal iterator operation, this method exposes {@link #nextPage()}
 * that allows the caller to retrieve items per page.
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
        fetch();
        return current!=null;
    }

    public T next() {
        fetch();
        if (current==null)  throw new NoSuchElementException();
        return current[pos++];
    }

    private void fetch() {
        while (current==null || current.length<=pos) {
            if (!base.hasNext()) {// no more to retrieve
                current = null;
                pos = 0;
                return;
            }

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
}
