package org.kohsuke.github;

/**
 * Hook event type.
 *
 * See http://developer.github.com/v3/events/types/
 *
 * @author Kohsuke Kawaguchi
 * @see GHEventInfo
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
    STATUS,
    TEAM_ADD,
    WATCH,
    PING
}
