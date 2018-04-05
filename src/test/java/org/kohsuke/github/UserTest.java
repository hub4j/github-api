/*
 * GitHub API for Java
 * Copyright (C) 2009-2017 SonarSource SA
 * mailto:info AT sonarsource DOT com
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

import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Kohsuke Kawaguchi
 */
public class UserTest extends AbstractGitHubApiTestBase {
    @Test
    public void listFollowsAndFollowers() throws IOException {
        GHUser u = gitHub.getUser("rtyler");
        assertNotEquals(
            count50(u.listFollowers()),
            count50(u.listFollows()));
    }

    private Set<GHUser> count50(PagedIterable<GHUser> l) {
        Set<GHUser> users = new HashSet<GHUser>();
        PagedIterator<GHUser> itr = l.iterator();
        for (int i=0; i<50 && itr.hasNext(); i++) {
            users.add(itr.next());
        }
        assertEquals(50, users.size());
        return users;
    }
}
