package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.io.Reader;
import java.time.Instant;
import java.util.ArrayList;
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
    /**
     * A check run event has been created, rerequested, completed, or has a requested_action.
     *
     * @see <a href="https://docs.github.com/en/developers/webhooks-and-events/webhook-events-and-payloads#check_run">
     *      check_run event</a>
     * @see <a href="https://docs.github.com/en/rest/reference/checks#check-runs">Check Runs</a>
     */
    public static class CheckRun extends GHEventPayload {

        private GHCheckRun checkRun;

        private int number;
        private GHRequestedAction requestedAction;
        /**
         * Create default CheckRun instance
         */
        public CheckRun() {
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
         * Gets number.
         *
         * @return the number
         */
        public int getNumber() {
            return number;
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
         * Create default CheckSuite instance
         */
        public CheckSuite() {
        }

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
     * Wrapper for changes on issue and pull request review comments action="edited".
     *
     * @see GHEventPayload.IssueComment
     * @see GHEventPayload.PullRequestReviewComment
     */
    @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "JSON API")
    public static class CommentChanges {

        /**
         * Wrapper for changed values.
         */
        public static class GHFrom {

            private String from;

            /**
             * Create default GHFrom instance
             */
            public GHFrom() {
            }

            /**
             * Previous comment value that was changed.
             *
             * @return previous value
             */
            public String getFrom() {
                return from;
            }
        }

        private GHFrom body;

        /**
         * Create default CommentChanges instance
         */
        public CommentChanges() {
        }

        /**
         * Gets the previous comment body.
         *
         * @return previous comment body (or null if not changed)
         */
        public GHFrom getBody() {
            return body;
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
         * Create default CommitComment instance
         */
        public CommitComment() {
        }

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

        private String description;

        private String masterBranch;
        private String ref;
        private String refType;
        /**
         * Create default Create instance
         */
        public Create() {
        }

        /**
         * Gets description.
         *
         * @return the description
         */
        public String getDescription() {
            return description;
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
         * Create default Delete instance
         */
        public Delete() {
        }

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
         * Create default Deployment instance
         */
        public Deployment() {
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

        private GHDeployment deployment;

        private GHDeploymentStatus deploymentStatus;
        /**
         * Create default DeploymentStatus instance
         */
        public DeploymentStatus() {
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
         * Gets deployment status.
         *
         * @return the deployment status
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHDeploymentStatus getDeploymentStatus() {
            return deploymentStatus;
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
         * Create default Discussion instance
         */
        public Discussion() {
        }

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

        private GHRepositoryDiscussionComment comment;

        private GHRepositoryDiscussion discussion;

        /**
         * Create default DiscussionComment instance
         */
        public DiscussionComment() {
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

        /**
         * Gets discussion.
         *
         * @return the discussion
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHRepositoryDiscussion getDiscussion() {
            return discussion;
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
         * Create default Fork instance
         */
        public Fork() {
        }

        /**
         * Gets forkee.
         *
         * @return the forkee
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHRepository getForkee() {
            return forkee;
        }
    }

    // List of events that still need to be added:
    // ContentReferenceEvent
    // DeployKeyEvent DownloadEvent FollowEvent ForkApplyEvent GitHubAppAuthorizationEvent GistEvent GollumEvent
    // InstallationEvent InstallationRepositoriesEvent IssuesEvent LabelEvent MarketplacePurchaseEvent MemberEvent
    // MembershipEvent MetaEvent MilestoneEvent OrganizationEvent OrgBlockEvent PackageEvent PageBuildEvent
    // ProjectCardEvent ProjectColumnEvent ProjectEvent RepositoryDispatchEvent RepositoryImportEvent
    // RepositoryVulnerabilityAlertEvent SecurityAdvisoryEvent StarEvent StatusEvent TeamEvent TeamAddEvent WatchEvent

    /**
     * An installation has been installed, uninstalled, or its permissions have been changed.
     *
     * @see <a href=
     *      "https://docs.github.com/en/developers/webhooks-and-events/webhook-events-and-payloads#installation">
     *      installation event</a>
     * @see <a href="https://docs.github.com/en/rest/reference/apps#installations">GitHub App Installation</a>
     */
    public static class Installation extends GHEventPayload {

        /**
         * A special minimal implementation of a {@link GHRepository} which contains only fields from "Properties of
         * repositories" from <a href=
         * "https://docs.github.com/en/webhooks-and-events/webhooks/webhook-events-and-payloads#installation">here</a>
         */
        public static class Repository {

            private String fullName;

            private long id;
            @JsonProperty(value = "private")
            private boolean isPrivate;
            private String name;
            private String nodeId;
            /**
             * Create default Repository instance
             */
            public Repository() {
            }

            /**
             * Gets the full name.
             *
             * @return the full name
             */
            public String getFullName() {
                return fullName;
            }

            /**
             * Get the id.
             *
             * @return the id
             */
            public long getId() {
                return id;
            }

            /**
             * Gets the name.
             *
             * @return the name
             */
            public String getName() {
                return name;
            }

            /**
             * Gets the node id.
             *
             * @return the node id
             */
            public String getNodeId() {
                return nodeId;
            }

            /**
             * Gets the repository private flag.
             *
             * @return whether the repository is private
             */
            public boolean isPrivate() {
                return isPrivate;
            }
        }

        private List<GHRepository> ghRepositories = null;
        private List<Repository> repositories;

        /**
         * Create default Installation instance
         */
        public Installation() {
        }

        /**
         * Returns a list of raw, unpopulated repositories. Useful when calling from within Installation event with
         * action "deleted". You can't fetch the info for repositories of an already deleted installation.
         *
         * @return the list of raw Repository records
         */
        public List<Repository> getRawRepositories() {
            return Collections.unmodifiableList(repositories);
        }

        /**
         * Gets repositories. For the "deleted" action please rather call {@link #getRawRepositories()}
         *
         * @return the repositories
         */
        public List<GHRepository> getRepositories() {
            if ("deleted".equalsIgnoreCase(getAction())) {
                throw new IllegalStateException("Can't call #getRepositories() on Installation event "
                        + "with 'deleted' action. Call #getRawRepositories() instead.");
            }

            if (ghRepositories == null) {
                ghRepositories = new ArrayList<>(repositories.size());
                try {
                    for (Repository singleRepo : repositories) {
                        // populate each repository
                        // the repository information provided here is so limited
                        // as to be unusable without populating, so we do it eagerly
                        ghRepositories.add(this.root().getRepositoryById(singleRepo.getId()));
                    }
                } catch (IOException e) {
                    throw new GHException("Failed to refresh repositories", e);
                }
            }

            return Collections.unmodifiableList(ghRepositories);
        }

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

        private List<GHRepository> repositoriesAdded;

        private List<GHRepository> repositoriesRemoved;
        private String repositorySelection;
        /**
         * Create default InstallationRepositories instance
         */
        public InstallationRepositories() {
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
         * Gets installation selection.
         *
         * @return the installation selection
         */
        public String getRepositorySelection() {
            return repositorySelection;
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
     * A Issue has been assigned, unassigned, labeled, unlabeled, opened, edited, milestoned, demilestoned, closed, or
     * reopened.
     *
     * @see <a href="https://docs.github.com/en/developers/webhooks-and-events/webhook-events-and-payloads#issues">
     *      issues events</a>
     * @see <a href="https://docs.github.com/en/rest/reference/issues#comments">Issues Comments</a>
     */
    public static class Issue extends GHEventPayload {

        private GHIssueChanges changes;

        private GHIssue issue;

        private GHLabel label;

        /**
         * Create default Issue instance
         */
        public Issue() {
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
         * Gets issue.
         *
         * @return the issue
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHIssue getIssue() {
            return issue;
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

        private CommentChanges changes;

        private GHIssueComment comment;
        private GHIssue issue;
        /**
         * Create default IssueComment instance
         */
        public IssueComment() {
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
         * Gets comment.
         *
         * @return the comment
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHIssueComment getComment() {
            return comment;
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
     * A label was created, edited or deleted.
     *
     * @see <a href= "https://docs.github.com/en/developers/webhooks-and-events/webhook-events-and-payloads#label">
     *      label event</a>
     */
    public static class Label extends GHEventPayload {

        private GHLabelChanges changes;

        private GHLabel label;

        /**
         * Create default Label instance
         */
        public Label() {
        }

        /**
         * Gets changes (for action="edited").
         *
         * @return changes
         */
        public GHLabelChanges getChanges() {
            return changes;
        }

        /**
         * Gets the label.
         *
         * @return the label
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
        public GHLabel getLabel() {
            return label;
        }
    }

    /**
     * A member event was triggered.
     *
     * @see <a href="https://docs.github.com/en/webhooks/webhook-events-and-payloads#member">member event</a>
     */
    public static class Member extends GHEventPayload {

        private GHMemberChanges changes;

        private GHUser member;

        /**
         * Create default Member instance
         */
        public Member() {
        }

        /**
         * Gets the changes made to the member.
         *
         * @return the changes made to the member
         */
        public GHMemberChanges getChanges() {
            return changes;
        }

        /**
         * Gets the member.
         *
         * @return the member
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHUser getMember() {
            return member;
        }
    }

    /**
     * A membership event was triggered.
     *
     * @see <a href="https://docs.github.com/en/webhooks/webhook-events-and-payloads#membership">membership event</a>
     */
    public static class Membership extends GHEventPayload {

        private GHUser member;

        private GHTeam team;

        /**
         * Create default Membership instance
         */
        public Membership() {
        }

        /**
         * Gets the member.
         *
         * @return the member
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHUser getMember() {
            return member;
        }

        /**
         * Gets the team.
         *
         * @return the team
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHTeam getTeam() {
            return team;
        }

        /**
         * Late bind.
         */
        @Override
        void lateBind() {
            if (team == null) {
                throw new IllegalStateException(
                        "Expected membership payload, but got something else. Maybe we've got another type of event?");
            }
            super.lateBind();
            GHOrganization organization = getOrganization();
            if (organization == null) {
                throw new IllegalStateException("Organization must not be null");
            }
            team.wrapUp(organization);
        }
    }

    /**
     * A ping.
     *
     * <a href="https://docs.github.com/en/developers/webhooks-and-events/webhook-events-and-payloads#ping"> ping
     * event</a>
     */
    public static class Ping extends GHEventPayload {

        /**
         * Create default Ping instance
         */
        public Ping() {
        }

    }

    /**
     * A project v2 item was archived, converted, created, edited, restored, deleted, or reordered.
     *
     * @see <a href=
     *      "https://docs.github.com/en/developers/webhooks-and-events/webhooks/webhook-events-and-payloads#projects_v2_item">projects_v2_item
     *      event</a>
     */
    public static class ProjectsV2Item extends GHEventPayload {

        private GHProjectsV2ItemChanges changes;

        private GHProjectsV2Item projectsV2Item;
        /**
         * Create default ProjectsV2Item instance
         */
        public ProjectsV2Item() {
        }

        /**
         * Gets the changes.
         *
         * @return the changes
         */
        public GHProjectsV2ItemChanges getChanges() {
            return changes;
        }

        /**
         * Gets the projects V 2 item.
         *
         * @return the projects V 2 item
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHProjectsV2Item getProjectsV2Item() {
            return projectsV2Item;
        }
    }

    /**
     * A repository was made public.
     *
     * @see <a href="https://docs.github.com/en/developers/webhooks-and-events/webhook-events-and-payloads#public">
     *      public event</a>
     */
    public static class Public extends GHEventPayload {

        /**
         * Create default Public instance
         */
        public Public() {
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

        private GHPullRequestChanges changes;

        private GHLabel label;
        private int number;
        private GHPullRequest pullRequest;
        /**
         * Create default PullRequest instance
         */
        public PullRequest() {
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
         * Gets the added or removed label for labeled/unlabeled events.
         *
         * @return label the added or removed label
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHLabel getLabel() {
            return label;
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
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHPullRequest getPullRequest() {
            return pullRequest;
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

        private GHPullRequest pullRequest;

        private GHPullRequestReview review;
        /**
         * Create default PullRequestReview instance
         */
        public PullRequestReview() {
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
         * Gets review.
         *
         * @return the review
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHPullRequestReview getReview() {
            return review;
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
     * A review comment was added to a pull request.
     *
     * @see <a href=
     *      "https://docs.github.com/en/developers/webhooks-and-events/webhook-events-and-payloads#pull_request_review_comment">
     *      pull_request_review_comment event</a>
     * @see <a href="https://docs.github.com/en/rest/reference/pulls#review-comments">Pull Request Review Comments</a>
     */
    public static class PullRequestReviewComment extends GHEventPayload {

        private CommentChanges changes;

        private GHPullRequestReviewComment comment;
        private GHPullRequest pullRequest;
        /**
         * Create default PullRequestReviewComment instance
         */
        public PullRequestReviewComment() {
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
         * Gets comment.
         *
         * @return the comment
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHPullRequestReviewComment getComment() {
            return comment;
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
     * A commit was pushed.
     *
     * @see <a href="https://docs.github.com/en/developers/webhooks-and-events/webhook-events-and-payloads#push"> push
     *      event</a>
     */
    public static class Push extends GHEventPayload {

        /**
         * Commit in a push. Note: sha is an alias for id.
         */
        public static class PushCommit {

            private List<String> added, removed, modified;

            private GitUser author;
            private GitUser committer;
            private boolean distinct;
            private String url, sha, message, timestamp;
            /**
             * Create default PushCommit instance
             */
            public PushCommit() {
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
             * Gets message.
             *
             * @return the message
             */
            public String getMessage() {
                return message;
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
             * Gets removed.
             *
             * @return the removed
             */
            public List<String> getRemoved() {
                return Collections.unmodifiableList(removed);
            }

            /**
             * Gets sha (id).
             *
             * @return the sha
             */
            public String getSha() {
                return sha;
            }

            /**
             * Obtains the timestamp of the commit.
             *
             * @return the timestamp
             */
            @WithBridgeMethods(value = Date.class, adapterMethod = "instantToDate")
            public Instant getTimestamp() {
                return GitHubClient.parseInstant(timestamp);
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
             * Whether this commit is distinct from any that have been pushed before.
             *
             * @return the boolean
             */
            public boolean isDistinct() {
                return distinct;
            }

            @JsonSetter
            private void setId(String id) {
                sha = id;
            }

        }

        /**
         * The type Pusher.
         */
        public static class Pusher {

            private String name, email;

            /**
             * Create default Pusher instance
             */
            public Pusher() {
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
             * Gets name.
             *
             * @return the name
             */
            public String getName() {
                return name;
            }
        }
        private List<PushCommit> commits;
        private String compare;
        private boolean created, deleted, forced;
        private String head, before;
        private PushCommit headCommit;
        private Pusher pusher;
        private String ref;

        private int size;

        /**
         * Create default Push instance
         */
        public Push() {
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

        /**
         * The list of pushed commits.
         *
         * @return the commits
         */
        public List<PushCommit> getCommits() {
            return Collections.unmodifiableList(commits);
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
         * The SHA of the HEAD commit on the repository.
         *
         * @return the head
         */
        public String getHead() {
            return head;
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

        @JsonSetter // alias
        private void setAfter(String after) {
            head = after;
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
         * Create default Release instance
         */
        public Release() {
        }

        /**
         * Gets release.
         *
         * @return the release
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHRelease getRelease() {
            return release;
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

        private GHRepositoryChanges changes;

        /**
         * Create default Repository instance
         */
        public Repository() {
        }

        /**
         * Get changes.
         *
         * @return GHRepositoryChanges
         */
        public GHRepositoryChanges getChanges() {
            return changes;
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
         * Create default Star instance
         */
        public Star() {
        }

        /**
         * Gets the date when the star is added. Is null when the star is deleted.
         *
         * @return the date when the star is added
         */
        @WithBridgeMethods(value = Date.class, adapterMethod = "instantToDate")
        public Instant getStarredAt() {
            return GitHubClient.parseInstant(starredAt);
        }
    }

    /**
     * A git commit status was changed.
     *
     * @see <a href="https://docs.github.com/en/developers/webhooks-and-events/webhook-events-and-payloads#status">
     *      status event</a>
     * @see <a href="https://docs.github.com/en/rest/reference/repos#statuses">Repository Statuses</a>
     */
    public static class Status extends GHEventPayload {

        private GHCommit commit;

        private String context;
        private String description;
        private GHCommitState state;
        private String targetUrl;
        /**
         * Create default Status instance
         */
        public Status() {
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
         * Gets the status content.
         *
         * @return status content
         */
        public String getContext() {
            return context;
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
         * The optional link added to the status.
         *
         * @return a url
         */
        public String getTargetUrl() {
            return targetUrl;
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
     * A team event was triggered.
     *
     * @see <a href="https://docs.github.com/en/webhooks/webhook-events-and-payloads#team">team event</a>
     */
    public static class Team extends GHEventPayload {

        private GHTeamChanges changes;

        private GHTeam team;

        /**
         * Create default Team instance
         */
        public Team() {
        }

        /**
         * Gets the changes made to the team.
         *
         * @return the changes made to the team, null unless action is "edited".
         */
        public GHTeamChanges getChanges() {
            return changes;
        }

        /**
         * Gets the team.
         *
         * @return the team
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHTeam getTeam() {
            return team;
        }

        /**
         * Late bind.
         */
        @Override
        void lateBind() {
            if (team == null) {
                throw new IllegalStateException(
                        "Expected team payload, but got something else. Maybe we've got another type of event?");
            }
            super.lateBind();
            GHOrganization organization = getOrganization();
            if (organization == null) {
                throw new IllegalStateException("Organization must not be null");
            }
            team.wrapUp(organization);
        }
    }

    /**
     * A team_add event was triggered.
     *
     * @see <a href="https://docs.github.com/en/webhooks/webhook-events-and-payloads#team_add">team_add event</a>
     */
    public static class TeamAdd extends GHEventPayload {

        private GHTeam team;

        /**
         * Create default TeamAdd instance
         */
        public TeamAdd() {
        }

        /**
         * Gets the team.
         *
         * @return the team
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHTeam getTeam() {
            return team;
        }

        /**
         * Late bind.
         */
        @Override
        void lateBind() {
            if (team == null) {
                throw new IllegalStateException(
                        "Expected team payload, but got something else. Maybe we've got another type of event?");
            }
            super.lateBind();
            GHOrganization organization = getOrganization();
            if (organization == null) {
                throw new IllegalStateException("Organization must not be null");
            }
            team.wrapUp(organization);
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
         * Create default WorkflowDispatch instance
         */
        public WorkflowDispatch() {
        }

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
         * Create default WorkflowJob instance
         */
        public WorkflowJob() {
        }

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
     * A workflow run was requested or completed.
     *
     * @see <a href=
     *      "https://docs.github.com/en/developers/webhooks-and-events/webhook-events-and-payloads#workflow_run">
     *      workflow run event</a>
     * @see <a href="https://docs.github.com/en/rest/reference/actions#workflow-runs">Actions Workflow Runs</a>
     */
    public static class WorkflowRun extends GHEventPayload {

        private GHWorkflow workflow;

        private GHWorkflowRun workflowRun;
        /**
         * Create default WorkflowRun instance
         */
        public WorkflowRun() {
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
         * Gets the workflow run.
         *
         * @return the workflow run
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
        public GHWorkflowRun getWorkflowRun() {
            return workflowRun;
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

    // https://docs.github.com/en/free-pro-team@latest/developers/webhooks-and-events/webhook-events-and-payloads#webhook-payload-object-common-properties
    // Webhook payload object common properties: action, sender, repository, organization, installation
    private String action;

    private GHAppInstallation installation;

    private GHOrganization organization;

    private GHRepository repository;

    private GHUser sender;

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
     * Gets installation.
     *
     * @return the installation
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
    public GHAppInstallation getInstallation() {
        return installation;
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
     * Gets repository.
     *
     * @return the repository
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
    public GHRepository getRepository() {
        return repository;
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
     * Late bind.
     */
    void lateBind() {
    }
}
