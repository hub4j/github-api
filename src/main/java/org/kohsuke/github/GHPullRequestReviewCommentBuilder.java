package org.kohsuke.github;

import java.io.IOException;

// TODO: Auto-generated Javadoc

/**
 * Builds up a creation of new {@link GHPullRequestReviewComment}.
 *
 * @see GHPullRequest#createReviewComment()
 */
public class GHPullRequestReviewCommentBuilder {
    private final Requester builder;
    private final GHPullRequest pr;

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

    /**
     * A single line of the blob in the pull request diff that the comment applies to.
     * <p>
     * {@link #line(int)} and {@link #lines(int, int)} will overwrite each other's values.
     * </p>
     *
     * @param line
     *            the line number
     * @return the gh pull request review comment builder
     */
    public GHPullRequestReviewCommentBuilder line(int line) {
        builder.with("line", line);
        builder.remove("start_line");
        return this;
    }

    /**
     * The range of lines in the pull request diff that this comment applies to.
     * <p>
     * {@link #line(int)} and {@link #lines(int, int)} will overwrite each other's values.
     * </p>
     *
     * @param startLine
     *            the start line number of the comment
     * @param endLine
     *            the end line number of the comment
     * @return the gh pull request review comment builder
     */
    public GHPullRequestReviewCommentBuilder lines(int startLine, int endLine) {
        builder.with("start_line", startLine);
        builder.with("line", endLine);
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
     * The side of the diff in the pull request that the comment applies to.
     * <p>
     * {@link #side(GHPullRequestReviewComment.Side)} and
     * {@link #sides(GHPullRequestReviewComment.Side, GHPullRequestReviewComment.Side)} will overwrite each other's
     * values.
     *
     * @param side
     *            side of the diff to which the comment applies
     * @return the gh pull request review comment builder
     */
    public GHPullRequestReviewCommentBuilder side(GHPullRequestReviewComment.Side side) {
        builder.with("side", side);
        builder.remove("start_side");
        return this;
    }

    /**
     * The sides of the diff in the pull request that the comment applies to.
     * <p>
     * {@link #side(GHPullRequestReviewComment.Side)} and
     * {@link #sides(GHPullRequestReviewComment.Side, GHPullRequestReviewComment.Side)} will overwrite each other's
     * values.
     *
     * @param startSide
     *            side of the diff to which the start of the comment applies
     * @param endSide
     *            side of the diff to which the end of the comment applies
     * @return the gh pull request review comment builder
     */
    public GHPullRequestReviewCommentBuilder sides(GHPullRequestReviewComment.Side startSide,
            GHPullRequestReviewComment.Side endSide) {
        builder.with("start_side", startSide);
        builder.with("side", endSide);
        return this;
    }

    /**
     * The position in the diff where you want to add a review comment.
     *
     * @param position
     *            the position
     * @return the gh pull request review comment builder
     * @implNote As position is deprecated in GitHub API, only keep this for internal usage (for retro-compatibility
     *           with {@link GHPullRequest#createReviewComment(String, String, String, int)}).
     */
    GHPullRequestReviewCommentBuilder position(int position) {
        builder.with("position", position);
        return this;
    }

}
