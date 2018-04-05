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
 * Action to perform on {@link GHPullRequestReview}.
 */
public enum GHPullRequestReviewEvent {
    PENDING,
    APPROVE,
    REQUEST_CHANGES,
    COMMENT;

    /*package*/ String action() {
        return this==PENDING ? null : name();
    }

    /**
     * When a {@link GHPullRequestReview} is submitted with this event, it should transition to this state.
     */
    /*package*/ GHPullRequestReviewState toState() {
        switch (this) {
        case PENDING:           return GHPullRequestReviewState.PENDING;
        case APPROVE:           return GHPullRequestReviewState.APPROVED;
        case REQUEST_CHANGES:   return GHPullRequestReviewState.CHANGES_REQUESTED;
        case COMMENT:           return GHPullRequestReviewState.COMMENTED;
        }
        throw new IllegalStateException();
    }
}
