package org.kohsuke.github;

import java.util.Locale;

// TODO: Auto-generated Javadoc
/**
 * Hook event type.
 *
 * @author Kohsuke Kawaguchi
 * @see GHEventInfo
 * @see <a href="https://developer.github.com/v3/activity/events/types/">Event type reference</a>
 */
public enum GHEvent {
    
    /** The branch protection rule. */
    BRANCH_PROTECTION_RULE,
    
    /** The check run. */
    CHECK_RUN,
    
    /** The check suite. */
    CHECK_SUITE,
    
    /** The code scanning alert. */
    CODE_SCANNING_ALERT,
    
    /** The commit comment. */
    COMMIT_COMMENT,
    
    /** The content reference. */
    CONTENT_REFERENCE,
    
    /** The create. */
    CREATE,
    
    /** The delete. */
    DELETE,
    
    /** The deploy key. */
    DEPLOY_KEY,
    
    /** The deployment. */
    DEPLOYMENT,
    
    /** The deployment status. */
    DEPLOYMENT_STATUS,
    
    /** The discussion. */
    DISCUSSION,
    
    /** The discussion comment. */
    DISCUSSION_COMMENT,
    
    /** The download. */
    DOWNLOAD,
    
    /** The follow. */
    FOLLOW,
    
    /** The fork. */
    FORK,
    
    /** The fork apply. */
    FORK_APPLY,
    
    /** The github app authorization. */
    GITHUB_APP_AUTHORIZATION,
    
    /** The gist. */
    GIST,
    
    /** The gollum. */
    GOLLUM,
    
    /** The installation. */
    INSTALLATION,
    
    /** The installation repositories. */
    INSTALLATION_REPOSITORIES,
    
    /** The integration installation repositories. */
    INTEGRATION_INSTALLATION_REPOSITORIES,
    
    /** The issue comment. */
    ISSUE_COMMENT,
    
    /** The issues. */
    ISSUES,
    
    /** The label. */
    LABEL,
    
    /** The marketplace purchase. */
    MARKETPLACE_PURCHASE,
    
    /** The member. */
    MEMBER,
    
    /** The membership. */
    MEMBERSHIP,
    
    /** The merge queue entry. */
    MERGE_QUEUE_ENTRY,
    
    /** The meta. */
    META,
    
    /** The milestone. */
    MILESTONE,
    
    /** The organization. */
    ORGANIZATION,
    
    /** The org block. */
    ORG_BLOCK,
    
    /** The package. */
    PACKAGE,
    
    /** The page build. */
    PAGE_BUILD,
    
    /** The project card. */
    PROJECT_CARD,
    
    /** The project column. */
    PROJECT_COLUMN,
    
    /** The project. */
    PROJECT,
    
    /** The ping. */
    PING,
    
    /** The public. */
    PUBLIC,
    
    /** The pull request. */
    PULL_REQUEST,
    
    /** The pull request review. */
    PULL_REQUEST_REVIEW,
    
    /** The pull request review comment. */
    PULL_REQUEST_REVIEW_COMMENT,
    
    /** The pull request review thread. */
    PULL_REQUEST_REVIEW_THREAD,
    
    /** The push. */
    PUSH,
    
    /** The registry package. */
    REGISTRY_PACKAGE,
    
    /** The release. */
    RELEASE,
    
    /** The repository dispatch. */
    REPOSITORY_DISPATCH, 
 /** The repository. */
 // only valid for org hooks
    REPOSITORY,
    
    /** The repository import. */
    REPOSITORY_IMPORT,
    
    /** The repository vulnerability alert. */
    REPOSITORY_VULNERABILITY_ALERT,
    
    /** The schedule. */
    SCHEDULE,
    
    /** The security advisory. */
    SECURITY_ADVISORY,
    
    /** The star. */
    STAR,
    
    /** The status. */
    STATUS,
    
    /** The team. */
    TEAM,
    
    /** The team add. */
    TEAM_ADD,
    
    /** The watch. */
    WATCH,
    
    /** The workflow job. */
    WORKFLOW_JOB,
    
    /** The workflow dispatch. */
    WORKFLOW_DISPATCH,
    
    /** The workflow run. */
    WORKFLOW_RUN,

    /**
     * Special event type that means we haven't found an enum value corresponding to the event.
     */
    UNKNOWN,

    /** Special event type that means "every possible event". */
    ALL;

    /**
     * Returns GitHub's internal representation of this event.
     *
     * @return the string
     */
    String symbol() {
        if (this == ALL)
            return "*";
        return name().toLowerCase(Locale.ENGLISH);
    }
}
