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
    private final List<DraftReviewComment> comments = new ArrayList<DraftReviewComment>();

    /**
     * Instantiates a new GH pull request review builder.
     *
     * @param pr the pr
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
     *            The relative path to the file that necessitates a review comment.
     * @param path
     *            The position in the diff where you want to add a review comment. Note this value is not the same as
     *            the line number in the file. For help finding the position value, read the note below.
     * @param position
     *            Text of the review comment.
     * @return the gh pull request review builder
     */
    public GHPullRequestReviewBuilder comment(String body, String path, int position) {
        comments.add(new DraftReviewComment(body, path, position));
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

    private static class DraftReviewComment {
        private String body;
        private String path;
        private int position;

        DraftReviewComment(String body, String path, int position) {
            this.body = body;
            this.path = path;
            this.position = position;
        }

        /**
         * Gets body.
         *
         * @return the body
         */
        public String getBody() {
            return body;
        }

        /**
         * Gets path.
         *
         * @return the path
         */
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
}
