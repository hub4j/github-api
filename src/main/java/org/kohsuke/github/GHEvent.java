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
 * Hook event type.
 *
 * @author Kohsuke Kawaguchi
 * @see GHEventInfo
 * @see <a href="https://developer.github.com/v3/activity/events/types/">Event type reference</a>
 */
public enum GHEvent {
    COMMIT_COMMENT,
    CREATE,
    DELETE,
    DEPLOYMENT,
    DEPLOYMENT_STATUS,
    DOWNLOAD,
    FOLLOW,
    FORK,
    FORK_APPLY,
    GIST,
    GOLLUM,
    INSTALLATION,
    INSTALLATION_REPOSITORIES,
    ISSUE_COMMENT,
    ISSUES,
    LABEL,
    MARKETPLACE_PURCHASE,
    MEMBER,
    MEMBERSHIP,
    MILESTONE,
    ORGANIZATION,
    ORG_BLOCK,
    PAGE_BUILD,
    PROJECT_CARD,
    PROJECT_COLUMN,
    PROJECT,
    PUBLIC,
    PULL_REQUEST,
    PULL_REQUEST_REVIEW,
    PULL_REQUEST_REVIEW_COMMENT,
    PUSH,
    RELEASE,
    REPOSITORY, // only valid for org hooks
    STATUS,
    TEAM,
    TEAM_ADD,
    WATCH,
    PING,
    /**
     * Special event type that means "every possible event"
     */
    ALL;


    /**
     * Returns GitHub's internal representation of this event.
     */
    String symbol() {
        if (this==ALL)  return "*";
        return name().toLowerCase(Locale.ENGLISH);
    }
}
