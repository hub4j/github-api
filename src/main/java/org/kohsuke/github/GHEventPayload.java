package org.kohsuke.github;

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

    /*package*/ GHEventPayload() {
    }

    /*package*/ void wrapUp(GitHub root) {
        this.root = root;
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
                pull_request.wrap(repository);
            } else {
                pull_request.wrapUp(root);
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
     * A commit was pushed.
     *
     * @see <a href="http://developer.github.com/v3/activity/events/types/#pushevent">authoritative source</a>
     */
    public static class Push extends GHEventPayload {
        private String head, before;
        private String ref;
        private int size;
        private List<PushCommit> commits;
        private GHRepository repository;

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

        /**
         * The list of pushed commits.
         */
        public List<PushCommit> getCommits() {
            return commits;
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

        /**
         * Commit in a push
         */
        public static class PushCommit {
            private GitUser author;
            private String url, sha, message;
            private boolean distinct;

            public GitUser getAuthor() {
                return author;
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

            public String getMessage() {
                return message;
            }

            /**
             * Whether this commit is distinct from any that have been pushed before.
             */
            public boolean isDistinct() {
                return distinct;
            }
        }
    }
}
