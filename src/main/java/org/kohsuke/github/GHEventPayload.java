package org.kohsuke.github;

import java.io.Reader;

/**
 * Base type for types used in databinding of the event payload.
 * 
 * @see GitHub#parseEventPayload(Reader, Class)
 * @see GHEventInfo#getPayload(Class)
 */
public abstract class GHEventPayload {
    protected GitHub root;

    /*package*/ GHEventPayload() {
    }

    /*package*/ void wrapUp(GitHub root) {
        this.root = root;
    }

    public static class PullRequest extends GHEventPayload {
        private String action;
        private int number;
        private GHPullRequest pull_request;

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

        @Override
        void wrapUp(GitHub root) {
            super.wrapUp(root);
            pull_request.wrapUp(root);
        }
    }
	
    public static class IssueComment extends GHEventPayload {
        private String action;
        private GHIssueComment comment;
		private GHIssue issue;
		private GHRepository repository;

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
			repository.wrap(root);
            issue.wrap(repository);
			comment.wrapUp(issue);
        }
    }
}
