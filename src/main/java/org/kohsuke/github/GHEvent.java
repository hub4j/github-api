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
    ISSUE_COMMENT,
    ISSUES,
    MEMBER,
    PAGE_BUILD,
    PUBLIC,
    PULL_REQUEST,
    PULL_REQUEST_REVIEW_COMMENT,
    PUSH,
    RELEASE,
    REPOSITORY, // only valid for org hooks
    STATUS,
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
