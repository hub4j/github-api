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

/**
 * {@link GHContent} with license information.
 *
 * @author Kohsuke Kawaguchi
 * @see <a href="https://developer.github.com/v3/licenses/#get-a-repositorys-license">documentation</a>
 * @see GHRepository#getLicense()
 */
@Preview @Deprecated
class GHContentWithLicense extends GHContent {
    GHLicense license;

    @Override
    GHContentWithLicense wrap(GHRepository owner) {
        super.wrap(owner);
        if (license!=null)
            license.wrap(owner.root);
        return this;
    }
}
