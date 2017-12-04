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

class GHRepoHook extends GHHook {
    /**
     * Repository that the hook belongs to.
     */
    /*package*/ transient GHRepository repository;

    /*package*/ GHRepoHook wrap(GHRepository owner) {
        this.repository = owner;
        return this;
    }

    @Override
    GitHub getRoot() {
        return repository.root;
    }

    @Override
    String getApiRoute() {
        return String.format("/repos/%s/%s/hooks/%d", repository.getOwnerName(), repository.getName(), id);
    }
}
