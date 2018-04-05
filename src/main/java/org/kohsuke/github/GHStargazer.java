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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Date;

/**
 * A stargazer at a repository on GitHub.
 *
 * @author noctarius
 */
@SuppressFBWarnings(value = {"UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD"}, justification = "JSON API")
public class GHStargazer {

    private GHRepository repository;
    private String starred_at;
    private GHUser user;

    /**
     * Gets the repository that is stargazed
     *
     * @return the starred repository
     */
    public GHRepository getRepository() {
        return repository;
    }

    /**
     * Gets the date when the repository was starred, however old stars before
     * August 2012, will all show the date the API was changed to support starred_at.
     *
     * @return the date the stargazer was added
     */
    public Date getStarredAt() {
        return GitHub.parseDate(starred_at);
    }

    /**
     * Gets the user that starred the repository
     *
     * @return the stargazer user
     */
    public GHUser getUser() {
        return user;
    }

    void wrapUp(GHRepository repository) {
        this.repository = repository;
        user.wrapUp(repository.root);
    }
}
