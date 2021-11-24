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
    BRANCH_PROTECTION_RULE,
    CHECK_RUN,
    CHECK_SUITE,
    CODE_SCANNING_ALERT,
    COMMIT_COMMENT,
    CONTENT_REFERENCE,
    CREATE,
    DELETE,
    DEPLOY_KEY,
    DEPLOYMENT,
    DEPLOYMENT_STATUS,
    DISCUSSION,
    DISCUSSION_COMMENT,
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
    MERGE_QUEUE_ENTRY,
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
    PULL_REQUEST_REVIEW_THREAD,
    PUSH,
    REGISTRY_PACKAGE,
    RELEASE,
    REPOSITORY_DISPATCH, // only valid for org hooks
    REPOSITORY,
    REPOSITORY_IMPORT,
    REPOSITORY_VULNERABILITY_ALERT,
    SCHEDULE,
    SECURITY_ADVISORY,
    STAR,
    STATUS,
    TEAM,
    TEAM_ADD,
    WATCH,
    WORKFLOW_JOB,
    WORKFLOW_DISPATCH,
    WORKFLOW_RUN,

    /**
     * Special event type that means we haven't found an enum value corresponding to the event.
     */
    UNKNOWN,

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
