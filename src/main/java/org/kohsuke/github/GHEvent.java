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
    CHECK_RUN,
    CHECK_SUITE,
    COMMIT_COMMENT,
    CONTENT_REFERENCE,
    CREATE,
    DELETE,
    DEPLOY_KEY,
    DEPLOYMENT,
    DEPLOYMENT_STATUS,
    DOWNLOAD,
    FOLLOW,
    FORK,
    FORK_APPLY,
    GITHUB_APP_AUTHORIZATION,
    GIST,
    GOLLUM,
    INSTALLATION,
    INSTALLATION_REPOSITORIES,
    INTEGRATION_INSTALLATION_REPOSITORIES,
    ISSUE_COMMENT,
    ISSUES,
    LABEL,
    MARKETPLACE_PURCHASE,
    MEMBER,
    MEMBERSHIP,
    META,
    MILESTONE,
    ORGANIZATION,
    ORG_BLOCK,
    PACKAGE,
    PAGE_BUILD,
    PROJECT_CARD,
    PROJECT_COLUMN,
    PROJECT,
    PING,
    PUBLIC,
    PULL_REQUEST,
    PULL_REQUEST_REVIEW,
    PULL_REQUEST_REVIEW_COMMENT,
    PUSH,
    RELEASE,
    REPOSITORY_DISPATCH, // only valid for org hooks
    REPOSITORY,
    REPOSITORY_IMPORT,
    REPOSITORY_VULNERABILITY_ALERT,
    SECURITY_ADVISORY,
    STAR,
    STATUS,
    TEAM,
    TEAM_ADD,
    WATCH,

    /**
     * Special event type that means "every possible event"
     */
    ALL;

    /**
     * Returns GitHub's internal representation of this event.
     */
    String symbol() {
        if (this == ALL)
            return "*";
        return name().toLowerCase(Locale.ENGLISH);
    }
}
