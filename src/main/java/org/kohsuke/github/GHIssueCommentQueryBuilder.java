package org.kohsuke.github;

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
    private final Requester req;
    private final GHIssue issue;

    /**
     * Instantiates a new GH issue comment query builder.
     *
     * @param issue the issue
     */
    GHIssueCommentQueryBuilder(GHIssue issue) {
        this.issue = issue;
        this.req = issue.root().createRequest().withUrlPath(issue.getIssuesApiRoute() + "/comments");
    }

    /**
     * Only comments created/updated after this date will be returned.
     *
     * @param date
     *            the date
     * @return the query builder
     */
    public GHIssueCommentQueryBuilder since(Date date) {
        req.with("since", GitHubClient.printDate(date));
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
        return since(new Date(timestamp));
    }

    /**
     * Lists up the comments with the criteria added so far.
     *
     * @return the paged iterable
     */
    public PagedIterable<GHIssueComment> list() {
        return req.toIterable(GHIssueComment[].class, item -> item.wrapUp(issue));
    }
}
