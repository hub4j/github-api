package org.kohsuke.github;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * Builds up a creation of new {@link GHPullRequestReview}.
 *
 * @author Kohsuke Kawaguchi
 * @see GHPullRequest#createReview() GHPullRequest#createReview()
 */
public class GHPullRequestReviewBuilder {
    private final GHPullRequest pr;
    private final Requester builder;
    private final List<ReviewComment> comments = new ArrayList<ReviewComment>();

    /**
     * Instantiates a new GH pull request review builder.
     *
     * @param pr
     *            the pr
     */
    GHPullRequestReviewBuilder(GHPullRequest pr) {
        this.pr = pr;
        this.builder = pr.root().createRequest();
    }

    // public GHPullRequestReview createReview(@Nullable String commitId, String body, GHPullRequestReviewEvent event,
    // List<GHPullRequestReviewComment> comments) throws IOException

    /**
     * The SHA of the commit that needs a review. Not using the latest commit SHA may render your review comment
     * outdated if a subsequent commit modifies the line you specify as the position. Defaults to the most recent commit
     * in the pull request when you do not specify a value.
     *
     * @param commitId
     *            the commit id
     * @return the gh pull request review builder
     */
    public GHPullRequestReviewBuilder commitId(String commitId) {
        builder.with("commit_id", commitId);
        return this;
    }

    /**
     * Required when using REQUEST_CHANGES or COMMENT for the event parameter. The body text of the pull request review.
     *
     * @param body
     *            the body
     * @return the gh pull request review builder
     */
    public GHPullRequestReviewBuilder body(String body) {
        builder.with("body", body);
        return this;
    }

    /**
     * The review action you want to perform. The review actions include: APPROVE, REQUEST_CHANGES, or COMMENT. By
     * leaving this blank, you set the review action state to PENDING, which means you will need to
     * {@linkplain GHPullRequestReview#submit(String, GHPullRequestReviewEvent) submit the pull request review} when you
     * are ready.
     *
     * @param event
     *            the event
     * @return the gh pull request review builder
     */
    public GHPullRequestReviewBuilder event(GHPullRequestReviewEvent event) {
        builder.with("event", event.action());
        return this;
    }

    /**
     * Comment gh pull request review builder.
     *
     * @param body
     *            Text of the review comment.
     * @param path
     *            The relative path to the file that necessitates a review comment.
     * @param position
     *            The position in the diff where you want to add a review comment. Note this value is not the same as
     *            the line number in the file. For help finding the position value, read the note below.
     * @return the gh pull request review builder
     */
    public GHPullRequestReviewBuilder comment(String body, String path, int position) {
        comments.add(new DraftReviewComment(body, path, position));
        return this;
    }

    /**
     * Add a multi-line comment to the gh pull request review builder.
     *
     * @param body
     *            Text of the review comment.
     * @param path
     *            The relative path to the file that necessitates a review comment.
     * @param startLine
     *            The first line in the pull request diff that the multi-line comment applies to.
     * @param line
     *            The last line of the range that the comment applies to.
     * @return the gh pull request review builder
     */
    public GHPullRequestReviewBuilder multiLineComment(String body, String path, int startLine, int line) {
        this.comments.add(new MultilineDraftReviewComment(body, path, startLine, line));
        return this;
    }

    /**
     * Add a single line comment to the gh pull request review builder.
     *
     * @param body
     *            Text of the review comment.
     * @param path
     *            The relative path to the file that necessitates a review comment.
     * @param line
     *            The line of the blob in the pull request diff that the comment applies to.
     * @return the gh pull request review builder
     */
    public GHPullRequestReviewBuilder singleLineComment(String body, String path, int line) {
        this.comments.add(new SingleLineDraftReviewComment(body, path, line));
        return this;
    }

    /**
     * Create gh pull request review.
     *
     * @return the gh pull request review
     * @throws IOException
     *             the io exception
     */
    public GHPullRequestReview create() throws IOException {
        return builder.method("POST")
                .with("comments", comments)
                .withUrlPath(pr.getApiRoute() + "/reviews")
                .fetch(GHPullRequestReview.class)
                .wrapUp(pr);
    }

    /**
     * Common properties of the review comments, regardless of how the comment is positioned on the gh pull request.
     */
    private interface ReviewComment {
        /**
         * Gets body.
         *
         * @return the body.
         */
        String getBody();

        /**
         * Gets path.
         *
         * @return the path.
         */
        String getPath();
    }

    /**
     * Single line comment using the relative position in the diff.
     */
    private static class DraftReviewComment implements ReviewComment {
        private String body;
        private String path;
        private int position;

        DraftReviewComment(String body, String path, int position) {
            this.body = body;
            this.path = path;
            this.position = position;
        }

        public String getBody() {
            return body;
        }

        public String getPath() {
            return path;
        }

        /**
         * Gets position.
         *
         * @return the position
         */
        public int getPosition() {
            return position;
        }
    }

    /**
     * Multi-line comment.
     */
    private static class MultilineDraftReviewComment implements ReviewComment {
        private final String body;
        private final String path;
        private final int line;
        private final int start_line;

        MultilineDraftReviewComment(final String body, final String path, final int startLine, final int line) {
            this.body = body;
            this.path = path;
            this.line = line;
            this.start_line = startLine;
        }

        public String getBody() {
            return this.body;
        }

        public String getPath() {
            return this.path;
        }

        /**
         * Gets end line of the comment.
         *
         * @return the end line of the comment.
         */
        public int getLine() {
            return line;
        }

        /**
         * Gets start line of the comment.
         *
         * @return the start line of the comment.
         */
        public int getStartLine() {
            return start_line;
        }
    }

    /**
     * Single line comment.
     */
    private static class SingleLineDraftReviewComment implements ReviewComment {
        private final String body;
        private final String path;
        private final int line;

        SingleLineDraftReviewComment(final String body, final String path, final int line) {
            this.body = body;
            this.path = path;
            this.line = line;
        }

        public String getBody() {
            return this.body;
        }

        public String getPath() {
            return this.path;
        }

        /**
         * Gets line of the comment.
         *
         * @return the line of the comment.
         */
        public int getLine() {
            return line;
        }
    }
}
