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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Represents a tag in {@link GHRepository}
 *
 * @see GHRepository#listTags()
 */
@SuppressFBWarnings(value = {"UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", 
    "NP_UNWRITTEN_FIELD"}, justification = "JSON API")
public class GHTag {
    private GHRepository owner;
    private GitHub root;

    private String name;
    private GHCommit commit;

    /*package*/ GHTag wrap(GHRepository owner) {
        this.owner = owner;
        this.root = owner.root;
        if (commit!=null)
            commit.wrapUp(owner);
        return this;
    }

    public GHRepository getOwner() {
        return owner;
    }

    public GitHub getRoot() {
        return root;
    }

    public String getName() {
        return name;
    }

    public GHCommit getCommit() {
        return commit;
    }
}
