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

/**
 * Current state of {@link GHPullRequestReview}
 */
public enum GHPullRequestReviewState {
    PENDING,
    APPROVED,
    CHANGES_REQUESTED,
    /**
     * @deprecated
     *      This was the thing when this API was in preview, but it changed when it became public.
     *      Use {@link #CHANGES_REQUESTED}. Left here for compatibility.
     */
    REQUEST_CHANGES,
    COMMENTED,
    DISMISSED;

    /**
     * @deprecated
     *      This was an internal method accidentally exposed.
     *      Left here for compatibility.
     */
    public String action() {
        GHPullRequestReviewEvent e = toEvent();
        return e==null ? null : e.action();
    }

    /*package*/ GHPullRequestReviewEvent toEvent() {
        switch (this) {
        case PENDING:       return GHPullRequestReviewEvent.PENDING;
        case APPROVED:      return GHPullRequestReviewEvent.APPROVE;
        case CHANGES_REQUESTED: return GHPullRequestReviewEvent.REQUEST_CHANGES;
        case REQUEST_CHANGES:   return GHPullRequestReviewEvent.REQUEST_CHANGES;
        case COMMENTED:         return GHPullRequestReviewEvent.COMMENT;
        }
        return null;
    }
}
