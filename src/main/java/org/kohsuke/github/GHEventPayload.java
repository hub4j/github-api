package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonSetter;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.Reader;
import java.util.List;

/**
 * Base type for types used in databinding of the event payload.
 *
 * @see GitHub#parseEventPayload(Reader, Class) GitHub#parseEventPayload(Reader, Class)
 * @see GHEventInfo#getPayload(Class) GHEventInfo#getPayload(Class)
 */
@SuppressWarnings("UnusedDeclaration")
public abstract class GHEventPayload {
    protected GitHub root;

    private GHUser sender;

    GHEventPayload() {
    }

    /**
     * Gets the sender or {@code null} if accessed via the events API.
     *
     * @return the sender or {@code null} if accessed via the events API.
     */
    public GHUser getSender() {
        return sender;
    }

    /**
     * Sets sender.
     *
     * @param sender
     *            the sender
     */
    public void setSender(GHUser sender) {
        this.sender = sender;
    }

    void wrapUp(GitHub root) {
        this.root = root;
        if (sender != null) {
            sender.wrapUp(root);
        }
    }

    // List of events that still need to be added:
    // CheckRunEvent CheckSuiteEvent ContentReferenceEvent
    // DeployKeyEvent DownloadEvent FollowEvent ForkApplyEvent GitHubAppAuthorizationEvent GistEvent GollumEvent
    // InstallationEvent InstallationRepositoriesEvent IssuesEvent LabelEvent MarketplacePurchaseEvent MemberEvent
    // MembershipEvent MetaEvent MilestoneEvent OrganizationEvent OrgBlockEvent PackageEvent PageBuildEvent
    // ProjectCardEvent ProjectColumnEvent ProjectEvent RepositoryDispatchEvent RepositoryImportEvent
    // RepositoryVulnerabilityAlertEvent SecurityAdvisoryEvent StarEvent StatusEvent TeamEvent TeamAddEvent WatchEvent

    /**
     * A check run event has been created, rerequested, completed, or has a requested_action.
     *
     * @see <a href="https://developer.github.com/v3/activity/events/types/#checkrunevent">authoritative source</a>
     */
    @SuppressFBWarnings(
            value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD" },
            justification = "JSON API")
    public static class CheckRun extends GHEventPayload {
        private String action;
        private int number;
        private GHCheckRun checkRun;
        private GHRequestedAction requestedAction;
        private GHRepository repository;

        /**
         * Gets action.
         *
         * @return the action
         */
        public String getAction() {
            return action;
        }

        /**
         * Gets number.
         *
         * @return the number
         */
        public int getNumber() {
            return number;
        }

        /**
         * Sets Check Run object
         *
         * @param currentCheckRun
         *            the check run object
         */
        public void setCheckRun(GHCheckRun currentCheckRun) {
            this.checkRun = currentCheckRun;
        }

        /**
         * Gets Check Run object
         *
         * @return the current checkRun object
         */
        public GHCheckRun getCheckRun() {
            return checkRun;
        }

        /**
         * Sets the Requested Action object
         *
         * @param currentRequestedAction
         *            the current action
         */
        public void setCheckRun(GHRequestedAction currentRequestedAction) {
            this.requestedAction = currentRequestedAction;
        }

        /**
         * Gets the Requested Action object
         *
         * @return the requested action
         */
        public GHRequestedAction getRequestedAction() {
            return requestedAction;
        }

        /**
         * Gets repository.
         *
         * @return the repository
         */
        public GHRepository getRepository() {
            return repository;
        }

        @Override
        void wrapUp(GitHub root) {
            super.wrapUp(root);
            if (checkRun == null)
                throw new IllegalStateException(
                        "Expected check_run payload, but got something else. Maybe we've got another type of event?");
            if (repository != null) {
                repository.wrap(root);
                checkRun.wrap(repository);
            } else {
                checkRun.wrap(root);
            }
        }
    }

    /**
     * A pull request status has changed.
     *
     * @see <a href="http://developer.github.com/v3/activity/events/types/#pullrequestevent">authoritative source</a>
     */
    @SuppressFBWarnings(
            value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD" },
            justification = "JSON API")
    public static class PullRequest extends GHEventPayload {
        private String action;
        private int number;
        private GHPullRequest pull_request;
        private GHRepository repository;

        /**
         * Gets action.
         *
         * @return the action
         */
        public String getAction() {
            return action;
        }

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
        public GHPullRequest getPullRequest() {
            pull_request.root = root;
            return pull_request;
        }

        /**
         * Gets repository.
         *
         * @return the repository
         */
        public GHRepository getRepository() {
            return repository;
        }

        @Override
        void wrapUp(GitHub root) {
            super.wrapUp(root);
            if (pull_request == null)
                throw new IllegalStateException(
                        "Expected pull_request payload, but got something else. Maybe we've got another type of event?");
            if (repository != null) {
                repository.wrap(root);
                pull_request.wrapUp(repository);
            } else {
                pull_request.wrapUp(root);
            }
        }
    }

    /**
     * A review was added to a pull request
     *
     * @see <a href="https://developer.github.com/v3/activity/events/types/#pullrequestreviewevent">authoritative
     *      source</a>
     */
    public static class PullRequestReview extends GHEventPayload {
        private String action;
        private GHPullRequestReview review;
        private GHPullRequest pull_request;
        private GHRepository repository;

        /**
         * Gets action.
         *
         * @return the action
         */
        public String getAction() {
            return action;
        }

        /**
         * Gets review.
         *
         * @return the review
         */
        public GHPullRequestReview getReview() {
            return review;
        }

        /**
         * Gets pull request.
         *
         * @return the pull request
         */
        public GHPullRequest getPullRequest() {
            return pull_request;
        }

        /**
         * Gets repository.
         *
         * @return the repository
         */
        public GHRepository getRepository() {
            return repository;
        }

        @Override
        void wrapUp(GitHub root) {
            super.wrapUp(root);
            if (review == null)
                throw new IllegalStateException(
                        "Expected pull_request_review payload, but got something else. Maybe we've got another type of event?");

            review.wrapUp(pull_request);

            if (repository != null) {
                repository.wrap(root);
                pull_request.wrapUp(repository);
            } else {
                pull_request.wrapUp(root);
            }
        }
    }

    /**
     * A review comment was added to a pull request
     *
     * @see <a href="https://developer.github.com/v3/activity/events/types/#pullrequestreviewcommentevent">authoritative
     *      source</a>
     */
    public static class PullRequestReviewComment extends GHEventPayload {
        private String action;
        private GHPullRequestReviewComment comment;
        private GHPullRequest pull_request;
        private GHRepository repository;

        /**
         * Gets action.
         *
         * @return the action
         */
        public String getAction() {
            return action;
        }

        /**
         * Gets comment.
         *
         * @return the comment
         */
        public GHPullRequestReviewComment getComment() {
            return comment;
        }

        /**
         * Gets pull request.
         *
         * @return the pull request
         */
        public GHPullRequest getPullRequest() {
            return pull_request;
        }

        /**
         * Gets repository.
         *
         * @return the repository
         */
        public GHRepository getRepository() {
            return repository;
        }

        @Override
        void wrapUp(GitHub root) {
            super.wrapUp(root);
            if (comment == null)
                throw new IllegalStateException(
                        "Expected pull_request_review_comment payload, but got something else. Maybe we've got another type of event?");

            comment.wrapUp(pull_request);

            if (repository != null) {
                repository.wrap(root);
                pull_request.wrapUp(repository);
            } else {
                pull_request.wrapUp(root);
            }
        }
    }

    /**
     * A Issue has been assigned, unassigned, labeled, unlabeled, opened, edited, milestoned, demilestoned, closed, or
     * reopened.
     *
     * @see <a href="http://developer.github.com/v3/activity/events/types/#issueevent">authoritative source</a>
     */
    @SuppressFBWarnings(value = { "UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR", "NP_UNWRITTEN_FIELD" },
            justification = "Constructed by JSON deserialization")
    public static class Issue extends GHEventPayload {
        private String action;
        private GHIssue issue;
        private GHRepository repository;

        /**
         * Gets action.
         *
         * @return the action
         */
        @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Comes from JSON deserialization")
        public String getAction() {
            return action;
        }

        /**
         * Gets issue.
         *
         * @return the issue
         */
        public GHIssue getIssue() {
            return issue;
        }

        /**
         * Sets issue.
         *
         * @param issue
         *            the issue
         */
        public void setIssue(GHIssue issue) {
            this.issue = issue;
        }

        /**
         * Gets repository.
         *
         * @return the repository
         */
        public GHRepository getRepository() {
            return repository;
        }

        /**
         * Sets repository.
         *
         * @param repository
         *            the repository
         */
        public void setRepository(GHRepository repository) {
            this.repository = repository;
        }

        @Override
        void wrapUp(GitHub root) {
            super.wrapUp(root);
            if (repository != null) {
                repository.wrap(root);
                issue.wrap(repository);
            } else {
                issue.wrap(root);
            }
        }
    }

    /**
     * A comment was added to an issue
     *
     * @see <a href="http://developer.github.com/v3/activity/events/types/#issuecommentevent">authoritative source</a>
     */
    @SuppressFBWarnings(value = { "UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR", "NP_UNWRITTEN_FIELD" },
            justification = "Constructed by JSON deserialization")
    public static class IssueComment extends GHEventPayload {
        private String action;
        private GHIssueComment comment;
        private GHIssue issue;
        private GHRepository repository;

        /**
         * Gets action.
         *
         * @return the action
         */
        @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Comes from JSON deserialization")
        public String getAction() {
            return action;
        }

        /**
         * Gets comment.
         *
         * @return the comment
         */
        public GHIssueComment getComment() {
            return comment;
        }

        /**
         * Sets comment.
         *
         * @param comment
         *            the comment
         */
        public void setComment(GHIssueComment comment) {
            this.comment = comment;
        }

        /**
         * Gets issue.
         *
         * @return the issue
         */
        public GHIssue getIssue() {
            return issue;
        }

        /**
         * Sets issue.
         *
         * @param issue
         *            the issue
         */
        public void setIssue(GHIssue issue) {
            this.issue = issue;
        }

        /**
         * Gets repository.
         *
         * @return the repository
         */
        public GHRepository getRepository() {
            return repository;
        }

        /**
         * Sets repository.
         *
         * @param repository
         *            the repository
         */
        public void setRepository(GHRepository repository) {
            this.repository = repository;
        }

        @Override
        void wrapUp(GitHub root) {
            super.wrapUp(root);
            if (repository != null) {
                repository.wrap(root);
                issue.wrap(repository);
            } else {
                issue.wrap(root);
            }
            comment.wrapUp(issue);
        }
    }

    /**
     * A comment was added to a commit
     *
     * @see <a href="http://developer.github.com/v3/activity/events/types/#commitcommentevent">authoritative source</a>
     */
    @SuppressFBWarnings(value = { "UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR", "NP_UNWRITTEN_FIELD" },
            justification = "Constructed by JSON deserialization")
    public static class CommitComment extends GHEventPayload {
        private String action;
        private GHCommitComment comment;
        private GHRepository repository;

        /**
         * Gets action.
         *
         * @return the action
         */
        @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Comes from JSON deserialization")
        public String getAction() {
            return action;
        }

        /**
         * Gets comment.
         *
         * @return the comment
         */
        public GHCommitComment getComment() {
            return comment;
        }

        /**
         * Sets comment.
         *
         * @param comment
         *            the comment
         */
        public void setComment(GHCommitComment comment) {
            this.comment = comment;
        }

        /**
         * Gets repository.
         *
         * @return the repository
         */
        public GHRepository getRepository() {
            return repository;
        }

        /**
         * Sets repository.
         *
         * @param repository
         *            the repository
         */
        public void setRepository(GHRepository repository) {
            this.repository = repository;
        }

        @Override
        void wrapUp(GitHub root) {
            super.wrapUp(root);
            if (repository != null) {
                repository.wrap(root);
                comment.wrap(repository);
            }
        }
    }

    /**
     * A repository, branch, or tag was created
     *
     * @see <a href="http://developer.github.com/v3/activity/events/types/#createevent">authoritative source</a>
     */
    @SuppressFBWarnings(value = { "UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR", "NP_UNWRITTEN_FIELD" },
            justification = "Constructed by JSON deserialization")
    public static class Create extends GHEventPayload {
        private String ref;
        private String refType;
        private String masterBranch;
        private String description;
        private GHRepository repository;

        /**
         * Gets ref.
         *
         * @return the ref
         */
        @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Comes from JSON deserialization")
        public String getRef() {
            return ref;
        }

        /**
         * Gets ref type.
         *
         * @return the ref type
         */
        @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Comes from JSON deserialization")
        public String getRefType() {
            return refType;
        }

        /**
         * Gets master branch.
         *
         * @return the master branch
         */
        @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Comes from JSON deserialization")
        public String getMasterBranch() {
            return masterBranch;
        }

        /**
         * Gets description.
         *
         * @return the description
         */
        @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Comes from JSON deserialization")
        public String getDescription() {
            return description;
        }

        /**
         * Gets repository.
         *
         * @return the repository
         */
        public GHRepository getRepository() {
            return repository;
        }

        /**
         * Sets repository.
         *
         * @param repository
         *            the repository
         */
        public void setRepository(GHRepository repository) {
            this.repository = repository;
        }

        @Override
        void wrapUp(GitHub root) {
            super.wrapUp(root);
            if (repository != null) {
                repository.wrap(root);
            }
        }
    }

    /**
     * A branch, or tag was deleted
     *
     * @see <a href="http://developer.github.com/v3/activity/events/types/#deleteevent">authoritative source</a>
     */
    @SuppressFBWarnings(value = { "UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR", "NP_UNWRITTEN_FIELD" },
            justification = "Constructed by JSON deserialization")
    public static class Delete extends GHEventPayload {
        private String ref;
        private String refType;
        private GHRepository repository;

        /**
         * Gets ref.
         *
         * @return the ref
         */
        @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Comes from JSON deserialization")
        public String getRef() {
            return ref;
        }

        /**
         * Gets ref type.
         *
         * @return the ref type
         */
        @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Comes from JSON deserialization")
        public String getRefType() {
            return refType;
        }

        /**
         * Gets repository.
         *
         * @return the repository
         */
        public GHRepository getRepository() {
            return repository;
        }

        /**
         * Sets repository.
         *
         * @param repository
         *            the repository
         */
        public void setRepository(GHRepository repository) {
            this.repository = repository;
        }

        @Override
        void wrapUp(GitHub root) {
            super.wrapUp(root);
            if (repository != null) {
                repository.wrap(root);
            }
        }
    }

    /**
     * A deployment
     *
     * @see <a href="http://developer.github.com/v3/activity/events/types/#deploymentevent">authoritative source</a>
     */
    @SuppressFBWarnings(value = { "UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR", "NP_UNWRITTEN_FIELD" },
            justification = "Constructed by JSON deserialization")
    public static class Deployment extends GHEventPayload {
        private GHDeployment deployment;
        private GHRepository repository;

        /**
         * Gets deployment.
         *
         * @return the deployment
         */
        public GHDeployment getDeployment() {
            return deployment;
        }

        /**
         * Sets deployment.
         *
         * @param deployment
         *            the deployment
         */
        public void setDeployment(GHDeployment deployment) {
            this.deployment = deployment;
        }

        /**
         * Gets repository.
         *
         * @return the repository
         */
        public GHRepository getRepository() {
            return repository;
        }

        /**
         * Sets repository.
         *
         * @param repository
         *            the repository
         */
        public void setRepository(GHRepository repository) {
            this.repository = repository;
        }

        @Override
        void wrapUp(GitHub root) {
            super.wrapUp(root);
            if (repository != null) {
                repository.wrap(root);
                deployment.wrap(repository);
            }
        }
    }

    /**
     * A deployment
     *
     * @see <a href="http://developer.github.com/v3/activity/events/types/#deploymentstatusevent">authoritative
     *      source</a>
     */
    @SuppressFBWarnings(value = { "UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR", "NP_UNWRITTEN_FIELD" },
            justification = "Constructed by JSON deserialization")
    public static class DeploymentStatus extends GHEventPayload {
        private GHDeploymentStatus deploymentStatus;
        private GHDeployment deployment;
        private GHRepository repository;

        /**
         * Gets deployment status.
         *
         * @return the deployment status
         */
        public GHDeploymentStatus getDeploymentStatus() {
            return deploymentStatus;
        }

        /**
         * Sets deployment status.
         *
         * @param deploymentStatus
         *            the deployment status
         */
        public void setDeploymentStatus(GHDeploymentStatus deploymentStatus) {
            this.deploymentStatus = deploymentStatus;
        }

        /**
         * Gets deployment.
         *
         * @return the deployment
         */
        public GHDeployment getDeployment() {
            return deployment;
        }

        /**
         * Sets deployment.
         *
         * @param deployment
         *            the deployment
         */
        public void setDeployment(GHDeployment deployment) {
            this.deployment = deployment;
        }

        /**
         * Gets repository.
         *
         * @return the repository
         */
        public GHRepository getRepository() {
            return repository;
        }

        /**
         * Sets repository.
         *
         * @param repository
         *            the repository
         */
        public void setRepository(GHRepository repository) {
            this.repository = repository;
        }

        @Override
        void wrapUp(GitHub root) {
            super.wrapUp(root);
            if (repository != null) {
                repository.wrap(root);
                deployment.wrap(repository);
                deploymentStatus.wrap(repository);
            }
        }
    }

    /**
     * A user forked a repository
     *
     * @see <a href="http://developer.github.com/v3/activity/events/types/#forkevent">authoritative source</a>
     */
    @SuppressFBWarnings(value = { "UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR", "NP_UNWRITTEN_FIELD" },
            justification = "Constructed by JSON deserialization")
    public static class Fork extends GHEventPayload {
        private GHRepository forkee;
        private GHRepository repository;

        /**
         * Gets forkee.
         *
         * @return the forkee
         */
        public GHRepository getForkee() {
            return forkee;
        }

        /**
         * Sets forkee.
         *
         * @param forkee
         *            the forkee
         */
        public void setForkee(GHRepository forkee) {
            this.forkee = forkee;
        }

        /**
         * Gets repository.
         *
         * @return the repository
         */
        public GHRepository getRepository() {
            return repository;
        }

        /**
         * Sets repository.
         *
         * @param repository
         *            the repository
         */
        public void setRepository(GHRepository repository) {
            this.repository = repository;
        }

        @Override
        void wrapUp(GitHub root) {
            super.wrapUp(root);
            forkee.wrap(root);
            if (repository != null) {
                repository.wrap(root);
            }
        }
    }

    /**
     * A ping.
     */
    public static class Ping extends GHEventPayload {
        private GHRepository repository;
        private GHOrganization organization;

        /**
         * Sets repository.
         *
         * @param repository
         *            the repository
         */
        public void setRepository(GHRepository repository) {
            this.repository = repository;
        }

        /**
         * Gets repository.
         *
         * @return the repository
         */
        public GHRepository getRepository() {
            return repository;
        }

        /**
         * Gets organization.
         *
         * @return the organization
         */
        public GHOrganization getOrganization() {
            return organization;
        }

        /**
         * Sets organization.
         *
         * @param organization
         *            the organization
         */
        public void setOrganization(GHOrganization organization) {
            this.organization = organization;
        }

        @Override
        void wrapUp(GitHub root) {
            super.wrapUp(root);
            if (repository != null)
                repository.wrap(root);
            if (organization != null) {
                organization.wrapUp(root);
            }
        }

    }

    /**
     * A repository was made public.
     *
     * @see <a href="http://developer.github.com/v3/activity/events/types/#publicevent">authoritative source</a>
     */
    public static class Public extends GHEventPayload {
        private GHRepository repository;

        /**
         * Sets repository.
         *
         * @param repository
         *            the repository
         */
        public void setRepository(GHRepository repository) {
            this.repository = repository;
        }

        /**
         * Gets repository.
         *
         * @return the repository
         */
        public GHRepository getRepository() {
            return repository;
        }

        @Override
        void wrapUp(GitHub root) {
            super.wrapUp(root);
            if (repository != null)
                repository.wrap(root);
        }

    }

    /**
     * A commit was pushed.
     *
     * @see <a href="http://developer.github.com/v3/activity/events/types/#pushevent">authoritative source</a>
     */
    @SuppressFBWarnings(
            value = { "UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR", "NP_UNWRITTEN_FIELD", "UUF_UNUSED_FIELD" },
            justification = "Constructed by JSON deserialization")
    public static class Push extends GHEventPayload {
        private String head, before;
        private boolean created, deleted, forced;
        private String ref;
        private int size;
        private List<PushCommit> commits;
        private GHRepository repository;
        private Pusher pusher;

        /**
         * The SHA of the HEAD commit on the repository
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
         * The full Git ref that was pushed. Example: “refs/heads/master”
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
            return commits;
        }

        /**
         * Gets repository.
         *
         * @return the repository
         */
        public GHRepository getRepository() {
            return repository;
        }

        /**
         * Gets pusher.
         *
         * @return the pusher
         */
        public Pusher getPusher() {
            return pusher;
        }

        /**
         * Sets pusher.
         *
         * @param pusher
         *            the pusher
         */
        public void setPusher(Pusher pusher) {
            this.pusher = pusher;
        }

        @Override
        void wrapUp(GitHub root) {
            super.wrapUp(root);
            if (repository != null)
                repository.wrap(root);
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
             */
            public void setName(String name) {
                this.name = name;
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
             */
            public void setEmail(String email) {
                this.email = email;
            }
        }

        /**
         * Commit in a push
         */
        public static class PushCommit {
            private GitUser author;
            private GitUser committer;
            private String url, sha, message;
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
             * Gets sha.
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
                return added;
            }

            /**
             * Gets removed.
             *
             * @return the removed
             */
            public List<String> getRemoved() {
                return removed;
            }

            /**
             * Gets modified.
             *
             * @return the modified
             */
            public List<String> getModified() {
                return modified;
            }
        }
    }

    /**
     * A release was added to the repo
     *
     * @see <a href="http://developer.github.com/v3/activity/events/types/#releaseevent">authoritative source</a>
     */
    @SuppressFBWarnings(value = { "UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR", "NP_UNWRITTEN_FIELD" },
            justification = "Constructed by JSON deserialization")
    public static class Release extends GHEventPayload {
        private String action;
        private GHRelease release;
        private GHRepository repository;

        /**
         * Gets action.
         *
         * @return the action
         */
        @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Comes from JSON deserialization")
        public String getAction() {
            return action;
        }

        /**
         * Gets release.
         *
         * @return the release
         */
        public GHRelease getRelease() {
            return release;
        }

        /**
         * Sets release.
         *
         * @param release
         *            the release
         */
        public void setRelease(GHRelease release) {
            this.release = release;
        }

        /**
         * Gets repository.
         *
         * @return the repository
         */
        public GHRepository getRepository() {
            return repository;
        }

        /**
         * Sets repository.
         *
         * @param repository
         *            the repository
         */
        public void setRepository(GHRepository repository) {
            this.repository = repository;
        }

        @Override
        void wrapUp(GitHub root) {
            super.wrapUp(root);
            if (repository != null) {
                repository.wrap(root);
            }
        }
    }

    /**
     * A repository was created, deleted, made public, or made private.
     *
     * @see <a href="http://developer.github.com/v3/activity/events/types/#repositoryevent">authoritative source</a>
     */
    @SuppressFBWarnings(
            value = { "UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR", "NP_UNWRITTEN_FIELD", "UWF_UNWRITTEN_FIELD" },
            justification = "Constructed by JSON deserialization")
    public static class Repository extends GHEventPayload {
        private String action;
        private GHRepository repository;
        private GHOrganization organization;

        /**
         * Gets action.
         *
         * @return the action
         */
        public String getAction() {
            return action;
        }

        /**
         * Sets repository.
         *
         * @param repository
         *            the repository
         */
        public void setRepository(GHRepository repository) {
            this.repository = repository;
        }

        /**
         * Gets repository.
         *
         * @return the repository
         */
        public GHRepository getRepository() {
            return repository;
        }

        /**
         * Gets organization.
         *
         * @return the organization
         */
        public GHOrganization getOrganization() {
            return organization;
        }

        /**
         * Sets organization.
         *
         * @param organization
         *            the organization
         */
        public void setOrganization(GHOrganization organization) {
            this.organization = organization;
        }

        @Override
        void wrapUp(GitHub root) {
            super.wrapUp(root);
            repository.wrap(root);
            if (organization != null) {
                organization.wrapUp(root);
            }
        }

    }
}
