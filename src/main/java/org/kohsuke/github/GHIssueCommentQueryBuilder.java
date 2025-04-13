package org.kohsuke.github;

import java.time.Instant;
import java.util.Date;

// TODO: Auto-generated Javadoc
/**
 * Builds a query for listing comments on an issue.
 * <p>
 * Call various methods that set the filter criteria, then the {@link #list()} method to actually retrieve the comments.
 *
 * <pre>
 * GHIssue issue = ...;
 * for (GHIssueComment comment : issue.queryComments().since(x).list()) {
 *     ...
 * }
 * </pre>
 *
 * @author Yoann Rodiere
 * @see GHIssue#queryComments() GHIssue#queryComments()
 * @see <a href="https://docs.github.com/en/rest/issues/comments#list-issue-comments">List issue comments</a>
 */
public class GHIssueCommentQueryBuilder {
    private final GHIssue issue;
    private final Requester req;

    /**
     * Instantiates a new GH issue comment query builder.
     *
     * @param issue
     *            the issue
     */
    GHIssueCommentQueryBuilder(GHIssue issue) {
        this.issue = issue;
        this.req = issue.root().createRequest().withUrlPath(issue.getIssuesApiRoute() + "/comments");
    }

    /**
     * Lists up the comments with the criteria added so far.
     *
     * @return the paged iterable
     */
    public PagedIterable<GHIssueComment> list() {
        return req.toIterable(GHIssueComment[].class, item -> item.wrapUp(issue));
    }

    /**
     * Only comments created/updated after this date will be returned.
     *
     * @param date
     *            the date
     * @return the query builder
     * @deprecated Use {@link #since(Instant)}
     */
    @Deprecated
    public GHIssueCommentQueryBuilder since(Date date) {
        return since(GitHubClient.toInstantOrNull(date));
    }

    /**
     * Only comments created/updated after this date will be returned.
     *
     * @param date
     *            the date
     * @return the query builder
     */
    public GHIssueCommentQueryBuilder since(Instant date) {
        req.with("since", GitHubClient.printInstant(date));
        return this;
    }

    /**
     * Only comments created/updated after this timestamp will be returned.
     *
     * @param timestamp
     *            the timestamp
     * @return the query builder
     */
    public GHIssueCommentQueryBuilder since(long timestamp) {
        return since(Instant.ofEpochMilli(timestamp));
    }
}
