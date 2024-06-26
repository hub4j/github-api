package org.kohsuke.github;

import java.io.IOException;

// TODO: Auto-generated Javadoc

/**
 * Builds up a creation of new {@link GHPullRequestReviewComment}.
 *
 * @see GHPullRequest#createReviewComment()
 */
public class GHPullRequestReviewCommentBuilder {
    private final GHPullRequest pr;
    private final Requester builder;

    /**
     * Instantiates a new GH pull request review comment builder.
     *
     * @param pr
     *            the pr
     */
    GHPullRequestReviewCommentBuilder(GHPullRequest pr) {
        this.pr = pr;
        this.builder = pr.root().createRequest();
    }

    /**
     * The SHA of the commit that needs a review. Not using the latest commit SHA may render your review comment
     * outdated if a subsequent commit modifies the line you specify as the position. Defaults to the most recent commit
     * in the pull request when you do not specify a value.
     *
     * @param commitId
     *            the commit id
     * @return the gh pull request review comment builder
     */
    public GHPullRequestReviewCommentBuilder commitId(String commitId) {
        builder.with("commit_id", commitId);
        return this;
    }

    /**
     * The text of the pull request review comment.
     *
     * @param body
     *            the body
     * @return the gh pull request review comment builder
     */
    public GHPullRequestReviewCommentBuilder body(String body) {
        builder.with("body", body);
        return this;
    }

    /**
     * The relative path to the file that necessitates a comment.
     *
     * @param path
     *            the path
     * @return the gh pull request review comment builder
     */
    public GHPullRequestReviewCommentBuilder path(String path) {
        builder.with("path", path);
        return this;
    }

    /**
     * The position in the diff where you want to add a review comment.
     *
     * @param position
     *            the position
     * @return the gh pull request review comment builder
     * @deprecated This parameter is deprecated in GitHub API, use {@link #line(int)} instead.
     */
    @Deprecated
    public GHPullRequestReviewCommentBuilder position(int position) {
        builder.with("position", position);
        return this;
    }

    /**
     * For a single line comment, the line of the blob in the pull request diff that the comment applies to. For a
     * multi-line comment, the last line of the range that your comment applies to.
     *
     * @param line
     *            the line number
     * @return the gh pull request review comment builder
     */
    public GHPullRequestReviewCommentBuilder line(int line) {
        builder.with("line", line);
        return this;
    }

    /**
     * The first line in the pull request diff that your multi-line comment applies to.
     *
     * @param line
     *            the line number
     * @return the gh pull request review comment builder
     */
    public GHPullRequestReviewCommentBuilder startLine(int line) {
        builder.with("start_line", line);
        return this;
    }

    /**
     * Create gh pull request review comment.
     *
     * @return the gh pull request review comment builder
     * @throws IOException
     *             the io exception
     */
    public GHPullRequestReviewComment create() throws IOException {
        return builder.method("POST")
                .withUrlPath(pr.getApiRoute() + "/comments")
                .fetch(GHPullRequestReviewComment.class)
                .wrapUp(pr);
    }

}
