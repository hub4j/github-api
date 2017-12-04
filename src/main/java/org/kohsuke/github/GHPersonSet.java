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
import java.util.Collection;
import java.util.HashSet;

/**
 * Set of {@link GHPerson} with helper lookup methods.
 * 
 * @author Kohsuke Kawaguchi
 */
public class GHPersonSet<T extends GHPerson> extends HashSet<T> {
    private static final long serialVersionUID = 1L;
 
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
