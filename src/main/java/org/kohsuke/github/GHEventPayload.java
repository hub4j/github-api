package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonSetter;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * Base type for types used in databinding of the event payload.
 *
 * @see GitHub#parseEventPayload(Reader, Class) GitHub#parseEventPayload(Reader, Class)
 * @see GHEventInfo#getPayload(Class) GHEventInfo#getPayload(Class)
 * @see <a href="https://docs.github.com/en/developers/webhooks-and-events/webhook-events-and-payloads">Webhook events
 *      and payloads</a>
 */
@SuppressFBWarnings(value = { "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD" }, justification = "JSON API")
public abstract class GHEventPayload extends GitHubInteractiveObject {
    // https://docs.github.com/en/free-pro-team@latest/developers/webhooks-and-events/webhook-events-and-payloads#webhook-payload-object-common-properties
    // Webhook payload object common properties: action, sender, repository, organization, installation
    private String action;
    private GHUser sender;
    private GHRepository repository;
    private GHOrganization organization;
    private GHAppInstallation installation;

    /**
     * Instantiates a new GH event payload.
     */
    GHEventPayload() {
    }

    /**
     * Gets the action for the triggered event. Most but not all webhook payloads contain an action property that
     * contains the specific activity that triggered the event.
     *
     * @return event action
     */
    public String getAction() {
        return action;
    }

    /**
     * Gets the sender or {@code null} if accessed via the events API.
     *
     * @return the sender or {@code null} if accessed via the events API.
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
    public GHUser getSender() {
        return sender;
    }

    /**
     * Sets sender.
     *
     * @param sender
     *            the sender
     * @deprecated Do not use this method. It was added due to incomplete understanding of Jackson binding.
     */
    @Deprecated
    public void setSender(GHUser sender) {
        throw new RuntimeException("Do not use this method.");
    }

    /**
     * Gets repository.
     *
     * @return the repository
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
    public GHRepository getRepository() {
        return repository;
    }

    /**
     * Sets repository.
     *
     * @param repository
     *            the repository
     * @deprecated Do not use this method. It was added due to incomplete understanding of Jackson binding.
     */
    @Deprecated
    public void setRepository(GHRepository repository) {
        throw new RuntimeException("Do not use this method.");
    }

    /**
     * Gets organization.
     *
     * @return the organization
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
    public GHOrganization getOrganization() {
        return organization;
    }

    /**
     * Sets organization.
     *
     * @param organization
     *            the organization
     * @deprecated Do not use this method. It was added due to incomplete understanding of Jackson binding.
     */
    @Deprecated
    public void setOrganization(GHOrganization organization) {
        throw new RuntimeException("Do not use this method.");
    }

    /**
     * Gets installation.
     *
     * @return the installation
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
    public GHAppInstallation getInstallation() {
        return installation;
    }

    // List of events that still need to be added:
    // ContentReferenceEvent
    // DeployKeyEvent DownloadEvent FollowEvent ForkApplyEvent GitHubAppAuthorizationEvent GistEvent GollumEvent
    // InstallationEvent InstallationRepositoriesEvent IssuesEvent LabelEvent MarketplacePurchaseEvent MemberEvent
    // MembershipEvent MetaEvent MilestoneEvent OrganizationEvent OrgBlockEvent PackageEvent PageBuildEvent
    // ProjectCardEvent ProjectColumnEvent ProjectEvent RepositoryDispatchEvent RepositoryImportEvent
    // RepositoryVulnerabilityAlertEvent SecurityAdvisoryEvent StarEvent StatusEvent TeamEvent TeamAddEvent WatchEvent

    /**
     * Late bind.
     */
    void lateBind() {
    }

    /**
     * A check run event has been created, rerequested, completed, or has a requested_action.
     *
     * @see <a href="https://docs.github.com/en/developers/webhooks-and-events/webhook-events-and-payloads#check_run">
     *      check_run event</a>
     * @see <a href="https://docs.github.com/en/rest/reference/checks#check-runs">Check Runs</a>
     */
    public static class CheckRun extends GHEventPayload {
        private int number;
        private GHCheckRun checkRun;
        private GHRequestedAction requestedAction;

        /**
         * Gets number.
         *
         * @return the number
         */
        public int getNumber() {
            return number;
        }

        /**
         * Sets Check Run object.
         *
         * @param currentCheckRun
         *            the check run object
         * @deprecated Do not use this method. It was added due to incomplete understanding of Jackson binding.
         */
        @Deprecated
        public void setCheckRun(GHCheckRun currentCheckRun) {
            throw new RuntimeException("Do not use this method.");
        }

        /**
         * Gets Check Run object.
         *
         * @return the current checkRun object
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHCheckRun getCheckRun() {
            return checkRun;
        }

        /**
         * Sets the Requested Action object.
         *
         * @param currentRequestedAction
         *            the current action
         * @deprecated Do not use this method. It was added due to incomplete understanding of Jackson binding.
         */
        @Deprecated
        public void setRequestedAction(GHRequestedAction currentRequestedAction) {
            throw new RuntimeException("Do not use this method.");
        }

        /**
         * Gets the Requested Action object.
         *
         * @return the requested action
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHRequestedAction getRequestedAction() {
            return requestedAction;
        }

        /**
         * Late bind.
         */
        @Override
        void lateBind() {
            if (checkRun == null)
                throw new IllegalStateException(
                        "Expected check_run payload, but got something else. Maybe we've got another type of event?");
            super.lateBind();
            GHRepository repository = getRepository();
            if (repository != null) {
                checkRun.wrap(repository);
            } else {
                checkRun.wrap(root());
            }
        }
    }

    /**
     * A check suite event has been requested, rerequested or completed.
     *
     * @see <a href="https://docs.github.com/en/developers/webhooks-and-events/webhook-events-and-payloads#check_suite">
     *      check_suite event</a>
     * @see <a href="https://docs.github.com/en/rest/reference/checks#check-suites">Check Suites</a>
     */
    public static class CheckSuite extends GHEventPayload {
        private GHCheckSuite checkSuite;

        /**
         * Gets the Check Suite object.
         *
         * @return the Check Suite object
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHCheckSuite getCheckSuite() {
            return checkSuite;
        }

        /**
         * Late bind.
         */
        @Override
        void lateBind() {
            if (checkSuite == null)
                throw new IllegalStateException(
                        "Expected check_suite payload, but got something else. Maybe we've got another type of event?");
            super.lateBind();
            GHRepository repository = getRepository();
            if (repository != null) {
                checkSuite.wrap(repository);
            } else {
                checkSuite.wrap(root());
            }
        }
    }

    /**
     * An installation has been installed, uninstalled, or its permissions have been changed.
     *
     * @see <a href=
     *      "https://docs.github.com/en/developers/webhooks-and-events/webhook-events-and-payloads#installation">
     *      installation event</a>
     * @see <a href="https://docs.github.com/en/rest/reference/apps#installations">GitHub App Installation</a>
     */
    public static class Installation extends GHEventPayload {
        private List<GHRepository> repositories;

        /**
         * Gets repositories.
         *
         * @return the repositories
         */
        public List<GHRepository> getRepositories() {
            return Collections.unmodifiableList(repositories);
        };

        /**
         * Late bind.
         */
        @Override
        void lateBind() {
            if (getInstallation() == null) {
                throw new IllegalStateException(
                        "Expected installation payload, but got something else. Maybe we've got another type of event?");
            }
            super.lateBind();
            if (repositories != null && !repositories.isEmpty()) {
                try {
                    for (GHRepository singleRepo : repositories) {
                        // populate each repository
                        // the repository information provided here is so limited
                        // as to be unusable without populating, so we do it eagerly
                        singleRepo.populate();
                    }
                } catch (IOException e) {
                    throw new GHException("Failed to refresh repositories", e);
                }
            }
        }
    }

    /**
     * A repository has been added or removed from an installation.
     *
     * @see <a href=
     *      "https://docs.github.com/en/developers/webhooks-and-events/webhook-events-and-payloads#installation_repositories">
     *      installation_repositories event</a>
     * @see <a href="https://docs.github.com/en/rest/reference/apps#installations">GitHub App installation</a>
     */
    public static class InstallationRepositories extends GHEventPayload {
        private String repositorySelection;
        private List<GHRepository> repositoriesAdded;
        private List<GHRepository> repositoriesRemoved;

        /**
         * Gets installation selection.
         *
         * @return the installation selection
         */
        public String getRepositorySelection() {
            return repositorySelection;
        }

        /**
         * Gets repositories added.
         *
         * @return the repositories
         */
        public List<GHRepository> getRepositoriesAdded() {
            return Collections.unmodifiableList(repositoriesAdded);
        }

        /**
         * Gets repositories removed.
         *
         * @return the repositories
         */
        public List<GHRepository> getRepositoriesRemoved() {
            return Collections.unmodifiableList(repositoriesRemoved);
        }

        /**
         * Late bind.
         */
        @Override
        void lateBind() {
            if (getInstallation() == null) {
                throw new IllegalStateException(
                        "Expected installation_repositories payload, but got something else. Maybe we've got another type of event?");
            }
            super.lateBind();
            List<GHRepository> repositories;
            if ("added".equals(getAction()))
                repositories = repositoriesAdded;
            else // action == "removed"
                repositories = repositoriesRemoved;

            if (repositories != null && !repositories.isEmpty()) {
                try {
                    for (GHRepository singleRepo : repositories) { // warp each of the repository
                        singleRepo.populate();
                    }
                } catch (IOException e) {
                    throw new GHException("Failed to refresh repositories", e);
                }
            }
        }
    }

    /**
     * A pull request status has changed.
     *
     * @see <a href=
     *      "https://docs.github.com/en/developers/webhooks-and-events/webhook-events-and-payloads#pull_request">
     *      pull_request event</a>
     * @see <a href="https://docs.github.com/en/rest/reference/pulls">Pull Requests</a>
     */
    @SuppressFBWarnings(value = { "NP_UNWRITTEN_FIELD" }, justification = "JSON API")
    public static class PullRequest extends GHEventPayload {
        private int number;
        private GHPullRequest pullRequest;
        private GHLabel label;
        private GHPullRequestChanges changes;

        /**
         * Gets number.
         *
         * @return the number
         */
        public int getNumber() {
            return number;
        }

        /**
         * Gets pull request.
         *
         * @return the pull request
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHPullRequest getPullRequest() {
            return pullRequest;
        }

        /**
         * Gets the added or removed label for labeled/unlabeled events.
         *
         * @return label the added or removed label
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHLabel getLabel() {
            return label;
        }

        /**
         * Get changes (for action="edited").
         *
         * @return changes
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHPullRequestChanges getChanges() {
            return changes;
        }

        /**
         * Late bind.
         */
        @Override
        void lateBind() {
            if (pullRequest == null)
                throw new IllegalStateException(
                        "Expected pull_request payload, but got something else. Maybe we've got another type of event?");
            super.lateBind();
            GHRepository repository = getRepository();
            if (repository != null) {
                pullRequest.wrapUp(repository);
            }
        }
    }

    /**
     * A review was added to a pull request.
     *
     * @see <a href=
     *      "https://docs.github.com/en/developers/webhooks-and-events/webhook-events-and-payloads#pull_request_review">
     *      pull_request_review event</a>
     * @see <a href="https://docs.github.com/en/rest/reference/pulls#reviews">Pull Request Reviews</a>
     */
    public static class PullRequestReview extends GHEventPayload {
        private GHPullRequestReview review;
        private GHPullRequest pullRequest;

        /**
         * Gets review.
         *
         * @return the review
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHPullRequestReview getReview() {
            return review;
        }

        /**
         * Gets pull request.
         *
         * @return the pull request
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHPullRequest getPullRequest() {
            return pullRequest;
        }

        /**
         * Late bind.
         */
        @Override
        void lateBind() {
            if (review == null)
                throw new IllegalStateException(
                        "Expected pull_request_review payload, but got something else. Maybe we've got another type of event?");
            super.lateBind();

            review.wrapUp(pullRequest);

            GHRepository repository = getRepository();
            if (repository != null) {
                pullRequest.wrapUp(repository);
            }
        }
    }

    /**
     * Wrapper for changes on issue and pull request review comments action="edited".
     *
     * @see GHEventPayload.IssueComment
     * @see GHEventPayload.PullRequestReviewComment
     */
    @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "JSON API")
    public static class CommentChanges {

        private GHFrom body;

        /**
         * Gets the previous comment body.
         *
         * @return previous comment body (or null if not changed)
         */
        public GHFrom getBody() {
            return body;
        }

        /**
         * Wrapper for changed values.
         */
        public static class GHFrom {
            private String from;

            /**
             * Previous comment value that was changed.
             *
             * @return previous value
             */
            public String getFrom() {
                return from;
            }
        }
    }

    /**
     * A review comment was added to a pull request.
     *
     * @see <a href=
     *      "https://docs.github.com/en/developers/webhooks-and-events/webhook-events-and-payloads#pull_request_review_comment">
     *      pull_request_review_comment event</a>
     * @see <a href="https://docs.github.com/en/rest/reference/pulls#review-comments">Pull Request Review Comments</a>
     */
    public static class PullRequestReviewComment extends GHEventPayload {
        private GHPullRequestReviewComment comment;
        private GHPullRequest pullRequest;
        private CommentChanges changes;

        /**
         * Gets comment.
         *
         * @return the comment
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHPullRequestReviewComment getComment() {
            return comment;
        }

        /**
         * Get changes (for action="edited").
         *
         * @return changes
         */
        public CommentChanges getChanges() {
            return changes;
        }

        /**
         * Gets pull request.
         *
         * @return the pull request
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHPullRequest getPullRequest() {
            return pullRequest;
        }

        /**
         * Late bind.
         */
        @Override
        void lateBind() {
            if (comment == null)
                throw new IllegalStateException(
                        "Expected pull_request_review_comment payload, but got something else. Maybe we've got another type of event?");
            super.lateBind();
            comment.wrapUp(pullRequest);

            GHRepository repository = getRepository();
            if (repository != null) {
                pullRequest.wrapUp(repository);
            }
        }
    }

    /**
     * A Issue has been assigned, unassigned, labeled, unlabeled, opened, edited, milestoned, demilestoned, closed, or
     * reopened.
     *
     * @see <a href="https://docs.github.com/en/developers/webhooks-and-events/webhook-events-and-payloads#issues">
     *      issues events</a>
     * @see <a href="https://docs.github.com/en/rest/reference/issues#comments">Issues Comments</a>
     */
    public static class Issue extends GHEventPayload {
        private GHIssue issue;

        private GHLabel label;

        private GHIssueChanges changes;

        /**
         * Gets issue.
         *
         * @return the issue
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHIssue getIssue() {
            return issue;
        }

        /**
         * Sets issue.
         *
         * @param issue
         *            the issue
         * @deprecated Do not use this method. It was added due to incomplete understanding of Jackson binding.
         */
        @Deprecated
        public void setIssue(GHIssue issue) {
            throw new RuntimeException("Do not use this method.");
        }

        /**
         * Gets the added or removed label for labeled/unlabeled events.
         *
         * @return label the added or removed label
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHLabel getLabel() {
            return label;
        }

        /**
         * Get changes (for action="edited").
         *
         * @return changes
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHIssueChanges getChanges() {
            return changes;
        }

        /**
         * Late bind.
         */
        @Override
        void lateBind() {
            super.lateBind();
            GHRepository repository = getRepository();
            if (repository != null) {
                issue.wrap(repository);
            }
        }
    }

    /**
     * A comment was added to an issue.
     *
     * @see <a href=
     *      "https://docs.github.com/en/developers/webhooks-and-events/webhook-events-and-payloads#issue_comment">
     *      issue_comment event</a>
     * @see <a href="https://docs.github.com/en/rest/reference/issues#comments">Issue Comments</a>
     */
    public static class IssueComment extends GHEventPayload {
        private GHIssueComment comment;
        private GHIssue issue;
        private CommentChanges changes;

        /**
         * Gets comment.
         *
         * @return the comment
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHIssueComment getComment() {
            return comment;
        }

        /**
         * Get changes (for action="edited").
         *
         * @return changes
         */
        public CommentChanges getChanges() {
            return changes;
        }

        /**
         * Sets comment.
         *
         * @param comment
         *            the comment
         * @deprecated Do not use this method. It was added due to incomplete understanding of Jackson binding.
         */
        @Deprecated
        public void setComment(GHIssueComment comment) {
            throw new RuntimeException("Do not use this method.");
        }

        /**
         * Gets issue.
         *
         * @return the issue
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHIssue getIssue() {
            return issue;
        }

        /**
         * Sets issue.
         *
         * @param issue
         *            the issue
         * @deprecated Do not use this method. It was added due to incomplete understanding of Jackson binding.
         */
        @Deprecated
        public void setIssue(GHIssue issue) {
            throw new RuntimeException("Do not use this method.");
        }

        /**
         * Late bind.
         */
        @Override
        void lateBind() {
            super.lateBind();
            GHRepository repository = getRepository();
            if (repository != null) {
                issue.wrap(repository);
            }
            comment.wrapUp(issue);
        }
    }

    /**
     * A comment was added to a commit.
     *
     * @see <a href=
     *      "https://docs.github.com/en/developers/webhooks-and-events/webhook-events-and-payloads#commit_comment">
     *      commit comment</a>
     * @see <a href="https://docs.github.com/en/rest/reference/repos#comments">Comments</a>
     */
    public static class CommitComment extends GHEventPayload {
        private GHCommitComment comment;

        /**
         * Gets comment.
         *
         * @return the comment
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHCommitComment getComment() {
            return comment;
        }

        /**
         * Sets comment.
         *
         * @param comment
         *            the comment
         * @deprecated Do not use this method. It was added due to incomplete understanding of Jackson binding.
         */
        @Deprecated
        public void setComment(GHCommitComment comment) {
            throw new RuntimeException("Do not use this method.");
        }

        /**
         * Late bind.
         */
        @Override
        void lateBind() {
            super.lateBind();
            GHRepository repository = getRepository();
            if (repository != null) {
                comment.wrap(repository);
            }
        }
    }

    /**
     * A repository, branch, or tag was created.
     *
     * @see <a href="https://docs.github.com/en/developers/webhooks-and-events/webhook-events-and-payloads#create">
     *      create event</a>
     * @see <a href="https://docs.github.com/en/rest/reference/git">Git data</a>
     */
    public static class Create extends GHEventPayload {
        private String ref;
        private String refType;
        private String masterBranch;
        private String description;

        /**
         * Gets ref.
         *
         * @return the ref
         */
        public String getRef() {
            return ref;
        }

        /**
         * Gets ref type.
         *
         * @return the ref type
         */
        public String getRefType() {
            return refType;
        }

        /**
         * Gets default branch.
         *
         * Name is an artifact of when "master" was the most common default.
         *
         * @return the default branch
         */
        public String getMasterBranch() {
            return masterBranch;
        }

        /**
         * Gets description.
         *
         * @return the description
         */
        public String getDescription() {
            return description;
        }
    }

    /**
     * A branch, or tag was deleted.
     *
     * @see <a href="https://docs.github.com/en/developers/webhooks-and-events/webhook-events-and-payloads#delete">
     *      delete event</a>
     * @see <a href="https://docs.github.com/en/rest/reference/git">Git data</a>
     */
    public static class Delete extends GHEventPayload {
        private String ref;
        private String refType;

        /**
         * Gets ref.
         *
         * @return the ref
         */
        public String getRef() {
            return ref;
        }

        /**
         * Gets ref type.
         *
         * @return the ref type
         */
        public String getRefType() {
            return refType;
        }
    }

    /**
     * A deployment.
     *
     * @see <a href="https://docs.github.com/en/developers/webhooks-and-events/webhook-events-and-payloads#deployment">
     *      deployment event</a>
     * @see <a href="https://docs.github.com/en/rest/reference/repos#deployments">Deployments</a>
     */
    public static class Deployment extends GHEventPayload {
        private GHDeployment deployment;

        /**
         * Gets deployment.
         *
         * @return the deployment
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHDeployment getDeployment() {
            return deployment;
        }

        /**
         * Sets deployment.
         *
         * @param deployment
         *            the deployment
         * @deprecated Do not use this method. It was added due to incomplete understanding of Jackson binding.
         */
        @Deprecated
        public void setDeployment(GHDeployment deployment) {
            throw new RuntimeException("Do not use this method.");
        }

        /**
         * Late bind.
         */
        @Override
        void lateBind() {
            super.lateBind();
            GHRepository repository = getRepository();
            if (repository != null) {
                deployment.wrap(repository);
            }
        }
    }

    /**
     * A deployment status.
     *
     * @see <a href=
     *      "https://docs.github.com/en/developers/webhooks-and-events/webhook-events-and-payloads#deployment_status">
     *      deployment_status event</a>
     * @see <a href="https://docs.github.com/en/rest/reference/repos#deployments">Deployments</a>
     */
    public static class DeploymentStatus extends GHEventPayload {
        private GHDeploymentStatus deploymentStatus;
        private GHDeployment deployment;

        /**
         * Gets deployment status.
         *
         * @return the deployment status
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHDeploymentStatus getDeploymentStatus() {
            return deploymentStatus;
        }

        /**
         * Sets deployment status.
         *
         * @param deploymentStatus
         *            the deployment status
         * @deprecated Do not use this method. It was added due to incomplete understanding of Jackson binding.
         */
        @Deprecated
        public void setDeploymentStatus(GHDeploymentStatus deploymentStatus) {
            throw new RuntimeException("Do not use this method.");
        }

        /**
         * Gets deployment.
         *
         * @return the deployment
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHDeployment getDeployment() {
            return deployment;
        }

        /**
         * Sets deployment.
         *
         * @param deployment
         *            the deployment
         * @deprecated Do not use this method. It was added due to incomplete understanding of Jackson binding.
         */
        @Deprecated
        public void setDeployment(GHDeployment deployment) {
            throw new RuntimeException("Do not use this method.");
        }

        /**
         * Late bind.
         */
        @Override
        void lateBind() {
            super.lateBind();
            GHRepository repository = getRepository();
            if (repository != null) {
                deployment.wrap(repository);
                deploymentStatus.lateBind(repository);
            }
        }
    }

    /**
     * A user forked a repository.
     *
     * @see <a href="https://docs.github.com/en/developers/webhooks-and-events/webhook-events-and-payloads#fork"> fork
     *      event</a>
     * @see <a href="https://docs.github.com/en/rest/reference/repos#forks">Forks</a>
     */
    public static class Fork extends GHEventPayload {
        private GHRepository forkee;

        /**
         * Gets forkee.
         *
         * @return the forkee
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHRepository getForkee() {
            return forkee;
        }

        /**
         * Sets forkee.
         *
         * @param forkee
         *            the forkee
         * @deprecated Do not use this method. It was added due to incomplete understanding of Jackson binding.
         */
        @Deprecated
        public void setForkee(GHRepository forkee) {
            throw new RuntimeException("Do not use this method.");
        }
    }

    /**
     * A ping.
     *
     * <a href="https://docs.github.com/en/developers/webhooks-and-events/webhook-events-and-payloads#ping"> ping
     * event</a>
     */
    public static class Ping extends GHEventPayload {
    }

    /**
     * A repository was made public.
     *
     * @see <a href="https://docs.github.com/en/developers/webhooks-and-events/webhook-events-and-payloads#public">
     *      public event</a>
     */
    public static class Public extends GHEventPayload {
    }

    /**
     * A commit was pushed.
     *
     * @see <a href="https://docs.github.com/en/developers/webhooks-and-events/webhook-events-and-payloads#push"> push
     *      event</a>
     */
    public static class Push extends GHEventPayload {
        private String head, before;
        private boolean created, deleted, forced;
        private String ref;
        private int size;
        private List<PushCommit> commits;
        private PushCommit headCommit;
        private Pusher pusher;
        private String compare;

        /**
         * The SHA of the HEAD commit on the repository.
         *
         * @return the head
         */
        public String getHead() {
            return head;
        }

        /**
         * This is undocumented, but it looks like this captures the commit that the ref was pointing to before the
         * push.
         *
         * @return the before
         */
        public String getBefore() {
            return before;
        }

        @JsonSetter // alias
        private void setAfter(String after) {
            head = after;
        }

        /**
         * The full Git ref that was pushed. Example: “refs/heads/main”
         *
         * @return the ref
         */
        public String getRef() {
            return ref;
        }

        /**
         * The number of commits in the push. Is this always the same as {@code getCommits().size()}?
         *
         * @return the size
         */
        public int getSize() {
            return size;
        }

        /**
         * Is created boolean.
         *
         * @return the boolean
         */
        public boolean isCreated() {
            return created;
        }

        /**
         * Is deleted boolean.
         *
         * @return the boolean
         */
        public boolean isDeleted() {
            return deleted;
        }

        /**
         * Is forced boolean.
         *
         * @return the boolean
         */
        public boolean isForced() {
            return forced;
        }

        /**
         * The list of pushed commits.
         *
         * @return the commits
         */
        public List<PushCommit> getCommits() {
            return Collections.unmodifiableList(commits);
        }

        /**
         * The head commit of the push.
         *
         * @return the commit
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public PushCommit getHeadCommit() {
            return headCommit;
        }

        /**
         * Gets pusher.
         *
         * @return the pusher
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public Pusher getPusher() {
            return pusher;
        }

        /**
         * Sets pusher.
         *
         * @param pusher
         *            the pusher
         * @deprecated Do not use this method. It was added due to incomplete understanding of Jackson binding.
         */
        @Deprecated
        public void setPusher(Pusher pusher) {
            throw new RuntimeException("Do not use this method.");
        }

        /**
         * Gets compare.
         *
         * @return compare
         */
        public String getCompare() {
            return compare;
        }

        /**
         * The type Pusher.
         */
        public static class Pusher {
            private String name, email;

            /**
             * Gets name.
             *
             * @return the name
             */
            public String getName() {
                return name;
            }

            /**
             * Sets name.
             *
             * @param name
             *            the name
             * @deprecated Do not use this method. It was added due to incomplete understanding of Jackson binding.
             */
            @Deprecated
            public void setName(String name) {
                throw new RuntimeException("Do not use this method.");
            }

            /**
             * Gets email.
             *
             * @return the email
             */
            public String getEmail() {
                return email;
            }

            /**
             * Sets email.
             *
             * @param email
             *            the email
             * @deprecated Do not use this method. It was added due to incomplete understanding of Jackson binding.
             */
            @Deprecated
            public void setEmail(String email) {
                throw new RuntimeException("Do not use this method.");
            }
        }

        /**
         * Commit in a push. Note: sha is an alias for id.
         */
        public static class PushCommit {
            private GitUser author;
            private GitUser committer;
            private String url, sha, message, timestamp;
            private boolean distinct;
            private List<String> added, removed, modified;

            /**
             * Gets author.
             *
             * @return the author
             */
            public GitUser getAuthor() {
                return author;
            }

            /**
             * Gets committer.
             *
             * @return the committer
             */
            public GitUser getCommitter() {
                return committer;
            }

            /**
             * Points to the commit API resource.
             *
             * @return the url
             */
            public String getUrl() {
                return url;
            }

            /**
             * Gets sha (id).
             *
             * @return the sha
             */
            public String getSha() {
                return sha;
            }

            @JsonSetter
            private void setId(String id) {
                sha = id;
            }

            /**
             * Gets message.
             *
             * @return the message
             */
            public String getMessage() {
                return message;
            }

            /**
             * Whether this commit is distinct from any that have been pushed before.
             *
             * @return the boolean
             */
            public boolean isDistinct() {
                return distinct;
            }

            /**
             * Gets added.
             *
             * @return the added
             */
            public List<String> getAdded() {
                return Collections.unmodifiableList(added);
            }

            /**
             * Gets removed.
             *
             * @return the removed
             */
            public List<String> getRemoved() {
                return Collections.unmodifiableList(removed);
            }

            /**
             * Gets modified.
             *
             * @return the modified
             */
            public List<String> getModified() {
                return Collections.unmodifiableList(modified);
            }

            /**
             * Obtains the timestamp of the commit.
             *
             * @return the timestamp
             */
            public Date getTimestamp() {
                return GitHubClient.parseDate(timestamp);
            }
        }
    }

    /**
     * A release was added to the repo.
     *
     * @see <a href="https://docs.github.com/en/developers/webhooks-and-events/webhook-events-and-payloads#release">
     *      release event</a>
     * @see <a href="https://docs.github.com/en/rest/reference/repos#releases">Releases</a>
     */
    @SuppressFBWarnings(value = { "UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR", "NP_UNWRITTEN_FIELD" },
            justification = "Constructed by JSON deserialization")
    public static class Release extends GHEventPayload {
        private GHRelease release;

        /**
         * Gets release.
         *
         * @return the release
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHRelease getRelease() {
            return release;
        }

        /**
         * Sets release.
         *
         * @param release
         *            the release
         * @deprecated Do not use this method. It was added due to incomplete understanding of Jackson binding.
         */
        @Deprecated
        public void setRelease(GHRelease release) {
            throw new RuntimeException("Do not use this method.");
        }
    }

    /**
     * A repository was created, deleted, made public, or made private.
     *
     * @see <a href="https://docs.github.com/en/developers/webhooks-and-events/webhook-events-and-payloads#repository">
     *      repository event</a>
     * @see <a href="https://docs.github.com/en/rest/reference/repos">Repositories</a>
     */
    public static class Repository extends GHEventPayload {

    }

    /**
     * A git commit status was changed.
     *
     * @see <a href="https://docs.github.com/en/developers/webhooks-and-events/webhook-events-and-payloads#status">
     *      status event</a>
     * @see <a href="https://docs.github.com/en/rest/reference/repos#statuses">Repository Statuses</a>
     */
    public static class Status extends GHEventPayload {
        private String context;
        private String description;
        private GHCommitState state;
        private GHCommit commit;
        private String targetUrl;

        /**
         * Gets the status content.
         *
         * @return status content
         */
        public String getContext() {
            return context;
        }

        /**
         * The optional link added to the status.
         *
         * @return a url
         */
        public String getTargetUrl() {
            return targetUrl;
        }

        /**
         * Gets the status description.
         *
         * @return status description
         */
        public String getDescription() {
            return description;
        }

        /**
         * Gets the status state.
         *
         * @return status state
         */
        public GHCommitState getState() {
            return state;
        }

        /**
         * Sets the status stage.
         *
         * @param state
         *            status state
         * @deprecated Do not use this method. It was added due to incomplete understanding of Jackson binding.
         */
        @Deprecated
        public void setState(GHCommitState state) {
            throw new RuntimeException("Do not use this method.");
        }

        /**
         * Gets the commit associated with the status event.
         *
         * @return commit
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHCommit getCommit() {
            return commit;
        }

        /**
         * Sets the commit associated with the status event.
         *
         * @param commit
         *            commit
         * @deprecated Do not use this method. It was added due to incomplete understanding of Jackson binding.
         */
        @Deprecated
        public void setCommit(GHCommit commit) {
            throw new RuntimeException("Do not use this method.");
        }

        /**
         * Late bind.
         */
        @Override
        void lateBind() {

            if (state == null) {
                throw new IllegalStateException(
                        "Expected status payload, but got something else. Maybe we've got another type of event?");
            }
            super.lateBind();

            GHRepository repository = getRepository();
            if (repository != null) {
                commit.wrapUp(repository);
            }
        }
    }

    /**
     * Occurs when someone triggered a workflow run or sends a POST request to the "Create a workflow dispatch event"
     * endpoint.
     *
     * @see <a href=
     *      "https://docs.github.com/en/developers/webhooks-and-events/webhook-events-and-payloads#workflow_dispatch">
     *      workflow dispatch event</a>
     * @see <a href=
     *      "https://docs.github.com/en/actions/reference/events-that-trigger-workflows#workflow_dispatch">Events that
     *      trigger workflows</a>
     */
    public static class WorkflowDispatch extends GHEventPayload {
        private Map<String, Object> inputs;
        private String ref;
        private String workflow;

        /**
         * Gets the map of input parameters passed to the workflow.
         *
         * @return the map of input parameters
         */
        public Map<String, Object> getInputs() {
            return Collections.unmodifiableMap(inputs);
        }

        /**
         * Gets the ref of the branch (e.g. refs/heads/main)
         *
         * @return the ref of the branch
         */
        public String getRef() {
            return ref;
        }

        /**
         * Gets the path of the workflow file (e.g. .github/workflows/hello-world-workflow.yml).
         *
         * @return the path of the workflow file
         */
        public String getWorkflow() {
            return workflow;
        }
    }

    /**
     * A workflow run was requested or completed.
     *
     * @see <a href=
     *      "https://docs.github.com/en/developers/webhooks-and-events/webhook-events-and-payloads#workflow_run">
     *      workflow run event</a>
     * @see <a href="https://docs.github.com/en/rest/reference/actions#workflow-runs">Actions Workflow Runs</a>
     */
    public static class WorkflowRun extends GHEventPayload {
        private GHWorkflowRun workflowRun;
        private GHWorkflow workflow;

        /**
         * Gets the workflow run.
         *
         * @return the workflow run
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHWorkflowRun getWorkflowRun() {
            return workflowRun;
        }

        /**
         * Gets the associated workflow.
         *
         * @return the associated workflow
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHWorkflow getWorkflow() {
            return workflow;
        }

        /**
         * Late bind.
         */
        @Override
        void lateBind() {
            if (workflowRun == null || workflow == null) {
                throw new IllegalStateException(
                        "Expected workflow and workflow_run payload, but got something else. Maybe we've got another type of event?");
            }
            super.lateBind();
            GHRepository repository = getRepository();
            if (repository == null) {
                throw new IllegalStateException("Repository must not be null");
            }
            workflowRun.wrapUp(repository);
            workflow.wrapUp(repository);
        }
    }

    /**
     * A workflow job has been queued, is in progress, or has been completed.
     *
     * @see <a href=
     *      "https://docs.github.com/en/developers/webhooks-and-events/webhooks/webhook-events-and-payloads#workflow_job">
     *      workflow job event</a>
     * @see <a href="https://docs.github.com/en/rest/reference/actions#workflow-jobs">Actions Workflow Jobs</a>
     */
    public static class WorkflowJob extends GHEventPayload {

        private GHWorkflowJob workflowJob;

        /**
         * Gets the workflow job.
         *
         * @return the workflow job
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHWorkflowJob getWorkflowJob() {
            return workflowJob;
        }

        /**
         * Late bind.
         */
        @Override
        void lateBind() {
            if (workflowJob == null) {
                throw new IllegalStateException(
                        "Expected workflow_job payload, but got something else.  Maybe we've got another type of event?");
            }
            super.lateBind();
            GHRepository repository = getRepository();
            if (repository == null) {
                throw new IllegalStateException("Repository must not be null");
            }
            workflowJob.wrapUp(repository);
        }
    }

    /**
     * A label was created, edited or deleted.
     *
     * @see <a href= "https://docs.github.com/en/developers/webhooks-and-events/webhook-events-and-payloads#label">
     *      label event</a>
     */
    public static class Label extends GHEventPayload {

        private GHLabel label;

        private GHLabelChanges changes;

        /**
         * Gets the label.
         *
         * @return the label
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
        public GHLabel getLabel() {
            return label;
        }

        /**
         * Gets changes (for action="edited").
         *
         * @return changes
         */
        public GHLabelChanges getChanges() {
            return changes;
        }
    }

    /**
     * A discussion was closed, reopened, created, edited, deleted, pinned, unpinned, locked, unlocked, transferred,
     * category_changed, answered, or unanswered.
     *
     * @see <a href=
     *      "https://docs.github.com/en/developers/webhooks-and-events/webhooks/webhook-events-and-payloads#discussion">
     *      discussion event</a>
     */
    public static class Discussion extends GHEventPayload {

        private GHRepositoryDiscussion discussion;

        private GHLabel label;

        /**
         * Gets discussion.
         *
         * @return the discussion
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHRepositoryDiscussion getDiscussion() {
            return discussion;
        }

        /**
         * Gets the added or removed label for labeled/unlabeled events.
         *
         * @return label the added or removed label
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHLabel getLabel() {
            return label;
        }
    }

    /**
     * A discussion comment was created, deleted, or edited.
     *
     * @see <a href=
     *      "https://docs.github.com/en/developers/webhooks-and-events/webhooks/webhook-events-and-payloads#discussion_comment">
     *      discussion event</a>
     */
    public static class DiscussionComment extends GHEventPayload {

        private GHRepositoryDiscussion discussion;

        private GHRepositoryDiscussionComment comment;

        /**
         * Gets discussion.
         *
         * @return the discussion
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHRepositoryDiscussion getDiscussion() {
            return discussion;
        }

        /**
         * Gets discussion comment.
         *
         * @return the discussion
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHRepositoryDiscussionComment getComment() {
            return comment;
        }
    }

    /**
     * A star was created or deleted on a repository.
     *
     * @see <a href=
     *      "https://docs.github.com/en/developers/webhooks-and-events/webhooks/webhook-events-and-payloads#star">star
     *      event</a>
     */
    public static class Star extends GHEventPayload {

        private String starredAt;

        /**
         * Gets the date when the star is added. Is null when the star is deleted.
         *
         * @return the date when the star is added
         */
        public Date getStarredAt() {
            return GitHubClient.parseDate(starredAt);
        }
    }

    /**
     * A project v2 item was archived, converted, created, edited, restored, deleted, or reordered.
     *
     * @see <a href=
     *      "https://docs.github.com/en/developers/webhooks-and-events/webhooks/webhook-events-and-payloads#projects_v2_item">star
     *      event</a>
     */
    public static class ProjectsV2Item extends GHEventPayload {

        private GHProjectsV2Item projectsV2Item;
        private GHProjectsV2ItemChanges changes;

        /**
         * Gets the projects V 2 item.
         *
         * @return the projects V 2 item
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHProjectsV2Item getProjectsV2Item() {
            return projectsV2Item;
        }

        /**
         * Gets the changes.
         *
         * @return the changes
         */
        public GHProjectsV2ItemChanges getChanges() {
            return changes;
        }
    }
}
