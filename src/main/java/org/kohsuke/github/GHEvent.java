package org.kohsuke.github;

import org.kohsuke.github.internal.EnumUtils;

import java.util.Locale;

import javax.annotation.Nonnull;

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
    CODE_SCANNING_ALERT,
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

    /**
     * Representation of GitHub Event Type
     *
     * @see <a href="https://docs.github.com/en/developers/webhooks-and-events/github-event-types">GitHub event
     *      types</a>
     */
    enum GitHubEventType {
        CommitCommentEvent(COMMIT_COMMENT),
        CreateEvent(CREATE),
        DeleteEvent(DELETE),
        ForkEvent(FORK),
        GollumEvent(GOLLUM),
        IssueCommentEvent(ISSUE_COMMENT),
        IssuesEvent(ISSUES),
        MemberEvent(MEMBER),
        PublicEvent(PUBLIC),
        PullRequestEvent(PULL_REQUEST),
        PullRequestReviewEvent(PULL_REQUEST_REVIEW),
        PullRequestReviewCommentEvent(PULL_REQUEST_REVIEW_COMMENT),
        PushEvent(PUSH),
        ReleaseEvent(RELEASE),
        WatchEvent(WATCH),
        UnknownEvent(UNKNOWN);

        private final GHEvent event;
        GitHubEventType(GHEvent event) {
            this.event = event;
        }

        /**
         * Required due to different naming conventions between different GitHub event names for Webhook events and
         * GitHub events
         *
         * @param event
         *            the github event as a string to convert to Event enum
         * @return GHEvent
         */
        static GHEvent transformToGHEvent(@Nonnull String event) {
            return EnumUtils.getEnumOrDefault(GitHubEventType.class, event, UnknownEvent).event;
        }
    }
}
