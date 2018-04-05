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

import java.io.IOException;

/**
 * Identifies a commit in {@link GHPullRequest}.
 *
 * @author Kohsuke Kawaguchi
 */
public class GHCommitPointer {
    private String ref, sha, label;
    private GHUser user;
    private GHRepository repo;

    /**
     * This points to the user who owns
     * the {@link #getRepository()}.
     */
    public GHUser getUser() throws IOException {
        if (user != null) return user.root.intern(user);
        return user;
    }

    /**
     * The repository that contains the commit.
     */
    public GHRepository getRepository() {
        return repo;
    }

    /**
     * Named ref to the commit. This appears to be a "short ref" that doesn't include "refs/heads/" portion.
     */
    public String getRef() {
        return ref;
    }

    /**
     * SHA1 of the commit.
     */
    public String getSha() {
        return sha;
    }

    /**
     * String that looks like "USERNAME:REF".
     */
    public String getLabel() {
        return label;
    }

    /**
     * Obtains the commit that this pointer is referring to.
     */
    public GHCommit getCommit() throws IOException {
        return getRepository().getCommit(getSha());
    }

    void wrapUp(GitHub root) {
        if (user!=null) user.root = root;
        if (repo!=null) repo.wrap(root);
    }
}
