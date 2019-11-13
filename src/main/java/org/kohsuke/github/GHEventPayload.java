package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.Reader;
import java.util.List;

/**
 * Base type for types used in databinding of the event payload.
 *
 * @see GitHub#parseEventPayload(Reader, Class)
 * @see GHEventInfo#getPayload(Class)
 */
@SuppressWarnings("UnusedDeclaration")
public abstract class GHEventPayload {
    protected GitHub root;

    private GHUser sender;

    /*package*/ GHEventPayload() {
    }

    /**
     * Gets the sender or {@code null} if accessed via the events API.
     * @return the sender or {@code null} if accessed via the events API.
     */
    public GHUser getSender() {
        return sender;
    }

    public void setSender(GHUser sender) {
        this.sender = sender;
    }

    /*package*/ void wrapUp(GitHub root) {
        this.root = root;
        if (sender != null) {
            sender.wrapUp(root);
        }
    }

    /**
     * A pull request status has changed.
     *
     * @see <a href="http://developer.github.com/v3/activity/events/types/#pullrequestevent">authoritative source</a>
     */
    @SuppressFBWarnings(value = {"UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD",
    "NP_UNWRITTEN_FIELD"}, justification = "JSON API")
    public static class PullRequest extends GHEventPayload {
        private String action;
        private int number;
        private GHPullRequest pull_request;
        private GHRepository repository;

        public String getAction() {
            return action;
        }

        public int getNumber() {
            return number;
        }

        public GHPullRequest getPullRequest() {
            pull_request.root = root;
            return pull_request;
        }

        public GHRepository getRepository() {
            return repository;
        }

        @Override
        void wrapUp(GitHub root) {
            super.wrapUp(root);
            if (pull_request==null)
                throw new IllegalStateException("Expected pull_request payload, but got something else. Maybe we've got another type of event?");
            if (repository!=null) {
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
     * @see <a href="https://developer.github.com/v3/activity/events/types/#pullrequestreviewevent">authoritative source</a>
     */
    public static class PullRequestReview extends GHEventPayload {
        private String action;
        private GHPullRequestReview review;
        private GHPullRequest pull_request;
        private GHRepository repository;

        public String getAction() {
            return action;
        }

        public GHPullRequestReview getReview() {
            return review;
        }

        public GHPullRequest getPullRequest() {
            return pull_request;
        }

        public GHRepository getRepository() {
            return repository;
        }

        @Override
        void wrapUp(GitHub root) {
            super.wrapUp(root);
            if (review==null)
                throw new IllegalStateException("Expected pull_request_review payload, but got something else. Maybe we've got another type of event?");

            review.wrapUp(pull_request);

            if (repository!=null) {
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
     * @see <a href="https://developer.github.com/v3/activity/events/types/#pullrequestreviewcommentevent">authoritative source</a>
     */
    public static class PullRequestReviewComment extends GHEventPayload {
        private String action;
        private GHPullRequestReviewComment comment;
        private GHPullRequest pull_request;
        private GHRepository repository;

        public String getAction() {
            return action;
        }

        public GHPullRequestReviewComment getComment() {
            return comment;
        }

        public GHPullRequest getPullRequest() {
            return pull_request;
        }

        public GHRepository getRepository() {
            return repository;
        }

        @Override
        void wrapUp(GitHub root) {
            super.wrapUp(root);
            if (comment==null)
                throw new IllegalStateException("Expected pull_request_review_comment payload, but got something else. Maybe we've got another type of event?");

            comment.wrapUp(pull_request);

            if (repository!=null) {
                repository.wrap(root);
                pull_request.wrapUp(repository);
            } else {
                pull_request.wrapUp(root);
            }
        }
    }

    /**
     * A Issue has been assigned, unassigned, labeled, unlabeled, opened, edited, milestoned, demilestoned, closed, or reopened.
     *
     * @see <a href="http://developer.github.com/v3/activity/events/types/#issueevent">authoritative source</a>
     */
    @SuppressFBWarnings(value = {"UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR", "NP_UNWRITTEN_FIELD" },
            justification = "Constructed by JSON deserialization")
    public static class Issue extends GHEventPayload {
        private String action;
        private GHIssue issue;
        private GHRepository repository;

        @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Comes from JSON deserialization")
        public String getAction() {
            return action;
        }

        public GHIssue getIssue() {
            return issue;
        }

        public void setIssue(GHIssue issue) {
            this.issue = issue;
        }

        public GHRepository getRepository() {
            return repository;
        }

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
    @SuppressFBWarnings(value = {"UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR", "NP_UNWRITTEN_FIELD" },
            justification = "Constructed by JSON deserialization")
    public static class IssueComment extends GHEventPayload {
        private String action;
        private GHIssueComment comment;
        private GHIssue issue;
        private GHRepository repository;

        @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Comes from JSON deserialization")
        public String getAction() {
            return action;
        }

        public GHIssueComment getComment() {
            return comment;
        }

        public void setComment(GHIssueComment comment) {
            this.comment = comment;
        }

        public GHIssue getIssue() {
            return issue;
        }

        public void setIssue(GHIssue issue) {
            this.issue = issue;
        }

        public GHRepository getRepository() {
            return repository;
        }

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
    @SuppressFBWarnings(value = {"UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR", "NP_UNWRITTEN_FIELD" },
            justification = "Constructed by JSON deserialization")
    public static class CommitComment extends GHEventPayload {
        private String action;
        private GHCommitComment comment;
        private GHRepository repository;

        @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Comes from JSON deserialization")
        public String getAction() {
            return action;
        }

        public GHCommitComment getComment() {
            return comment;
        }

        public void setComment(GHCommitComment comment) {
            this.comment = comment;
        }

        public GHRepository getRepository() {
            return repository;
        }

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
    @SuppressFBWarnings(value = {"UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR", "NP_UNWRITTEN_FIELD" },
            justification = "Constructed by JSON deserialization")
    public static class Create extends GHEventPayload {
        private String ref;
        @JsonProperty("ref_type")
        private String refType;
        @JsonProperty("master_branch")
        private String masterBranch;
        private String description;
        private GHRepository repository;

        @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Comes from JSON deserialization")
        public String getRef() {
            return ref;
        }

        @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Comes from JSON deserialization")
        public String getRefType() {
            return refType;
        }

        @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Comes from JSON deserialization")
        public String getMasterBranch() {
            return masterBranch;
        }

        @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Comes from JSON deserialization")
        public String getDescription() {
            return description;
        }

        public GHRepository getRepository() {
            return repository;
        }

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
    @SuppressFBWarnings(value = {"UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR", "NP_UNWRITTEN_FIELD" },
            justification = "Constructed by JSON deserialization")
    public static class Delete extends GHEventPayload {
        private String ref;
        @JsonProperty("ref_type")
        private String refType;
        private GHRepository repository;

        @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Comes from JSON deserialization")
        public String getRef() {
            return ref;
        }

        @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Comes from JSON deserialization")
        public String getRefType() {
            return refType;
        }

        public GHRepository getRepository() {
            return repository;
        }

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
    @SuppressFBWarnings(value = {"UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR", "NP_UNWRITTEN_FIELD" },
            justification = "Constructed by JSON deserialization")
    public static class Deployment extends GHEventPayload {
        private GHDeployment deployment;
        private GHRepository repository;

        public GHDeployment getDeployment() {
            return deployment;
        }

        public void setDeployment(GHDeployment deployment) {
            this.deployment = deployment;
        }

        public GHRepository getRepository() {
            return repository;
        }

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
     * @see <a href="http://developer.github.com/v3/activity/events/types/#deploymentstatusevent">authoritative source</a>
     */
    @SuppressFBWarnings(value = {"UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR", "NP_UNWRITTEN_FIELD" },
            justification = "Constructed by JSON deserialization")
    public static class DeploymentStatus extends GHEventPayload {
        @JsonProperty("deployment_status")
        private GHDeploymentStatus deploymentStatus;
        private GHDeployment deployment;
        private GHRepository repository;

        public GHDeploymentStatus getDeploymentStatus() {
            return deploymentStatus;
        }

        public void setDeploymentStatus(GHDeploymentStatus deploymentStatus) {
            this.deploymentStatus = deploymentStatus;
        }

        public GHDeployment getDeployment() {
            return deployment;
        }

        public void setDeployment(GHDeployment deployment) {
            this.deployment = deployment;
        }

        public GHRepository getRepository() {
            return repository;
        }

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
    @SuppressFBWarnings(value = {"UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR", "NP_UNWRITTEN_FIELD" },
            justification = "Constructed by JSON deserialization")
    public static class Fork extends GHEventPayload {
        private GHRepository forkee;
        private GHRepository repository;


        public GHRepository getForkee() {
            return forkee;
        }

        public void setForkee(GHRepository forkee) {
            this.forkee = forkee;
        }

        public GHRepository getRepository() {
            return repository;
        }

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

        public void setRepository(GHRepository repository) {
            this.repository = repository;
        }

        public GHRepository getRepository() {
            return repository;
        }

        public GHOrganization getOrganization() {
            return organization;
        }

        public void setOrganization(GHOrganization organization) {
            this.organization = organization;
        }

        @Override
        void wrapUp(GitHub root) {
            super.wrapUp(root);
            if (repository!=null)
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

        public void setRepository(GHRepository repository) {
            this.repository = repository;
        }

        public GHRepository getRepository() {
            return repository;
        }

        @Override
        void wrapUp(GitHub root) {
            super.wrapUp(root);
            if (repository!=null)
                repository.wrap(root);
        }

    }

    /**
     * A commit was pushed.
     *
     * @see <a href="http://developer.github.com/v3/activity/events/types/#pushevent">authoritative source</a>
     */
    @SuppressFBWarnings(value = {"UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR", "NP_UNWRITTEN_FIELD", "UUF_UNUSED_FIELD"},
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
         */
        public String getHead() {
            return head;
        }

        /**
         * This is undocumented, but it looks like this captures the commit that the ref was pointing to
         * before the push.
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
         */
        public String getRef() {
            return ref;
        }

        /**
         * The number of commits in the push.
         * Is this always the same as {@code getCommits().size()}?
         */
        public int getSize() {
            return size;
        }

        public boolean isCreated() {
            return created;
        }

        public boolean isDeleted() {
            return deleted;
        }

        public boolean isForced() {
            return forced;
        }

        /**
         * The list of pushed commits.
         */
        public List<PushCommit> getCommits() {
            return commits;
        }

        public GHRepository getRepository() {
            return repository;
        }

        public Pusher getPusher() {
            return pusher;
        }

        public void setPusher(Pusher pusher) {
            this.pusher = pusher;
        }

        @Override
        void wrapUp(GitHub root) {
            super.wrapUp(root);
            if (repository!=null)
                repository.wrap(root);
        }

        public static class Pusher {
            private String name, email;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getEmail() {
                return email;
            }

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

            public GitUser getAuthor() {
                return author;
            }

            public GitUser getCommitter() {
                return committer;
            }

            /**
             * Points to the commit API resource.
             */
            public String getUrl() {
                return url;
            }

            public String getSha() {
                return sha;
            }

            @JsonSetter
            private void setId(String id) {
                sha = id;
            }

            public String getMessage() {
                return message;
            }

            /**
             * Whether this commit is distinct from any that have been pushed before.
             */
            public boolean isDistinct() {
                return distinct;
            }

            public List<String> getAdded() {
                return added;
            }

            public List<String> getRemoved() {
                return removed;
            }

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
    @SuppressFBWarnings(value = {"UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR", "NP_UNWRITTEN_FIELD" },
            justification = "Constructed by JSON deserialization")
    public static class Release extends GHEventPayload {
        private String action;
        private GHRelease release;
        private GHRepository repository;

        @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Comes from JSON deserialization")
        public String getAction() {
            return action;
        }

        public GHRelease getRelease() {
            return release;
        }

        public void setRelease(GHRelease release) {
            this.release = release;
        }

        public GHRepository getRepository() {
            return repository;
        }

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
    @SuppressFBWarnings(value = {"UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR", "NP_UNWRITTEN_FIELD", "UWF_UNWRITTEN_FIELD"},
                        justification = "Constructed by JSON deserialization")
    public static class Repository extends GHEventPayload {
        private String action;
        private GHRepository repository;
        private GHOrganization organization;

        public String getAction() {
            return action;
        }

        public void setRepository(GHRepository repository) {
            this.repository = repository;
        }

        public GHRepository getRepository() {
            return repository;
        }

        public GHOrganization getOrganization() {
            return organization;
        }

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
