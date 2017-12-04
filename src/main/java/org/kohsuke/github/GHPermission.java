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

import java.util.Locale;

/**
 * Permission for a user in a repository.
 * @see <a href="https://developer.github.com/v3/repos/collaborators/#review-a-users-permission-level">API</a>
 */
/*package*/ class GHPermission {

    private String permission;
    private GHUser user;

    /**
     * @return one of {@code admin}, {@code write}, {@code read}, or {@code none}
     */
    public String getPermission() {
        return permission;
    }

    public GHPermissionType getPermissionType() {
        return Enum.valueOf(GHPermissionType.class, permission.toUpperCase(Locale.ENGLISH));
    }

    public GHUser getUser() {
        return user;
    }

    void wrapUp(GitHub root) {
        if (user != null) {
            user.root = root;
        }
    }

}
