/*
 * The MIT License
 *
 * Copyright (c) 2017, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.kohsuke.github;

import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.Date;

import javax.annotation.CheckForNull;

// TODO: Auto-generated Javadoc
/**
 * Review to a pull request.
 *
 * @see GHPullRequest#listReviews() GHPullRequest#listReviews()
 * @see GHPullRequestReviewBuilder
 */
@SuppressFBWarnings(value = { "UWF_UNWRITTEN_FIELD" }, justification = "JSON API")
public class GHPullRequestReview extends GHObject {

    /**
     * Represents a review comment as returned by the review comments endpoint. This is a limited view that does not
     * include line-related fields such as {@code line}, {@code originalLine}, {@code side}, etc.
     *
     * <p>
     * To obtain the full {@link GHPullRequestReviewComment} with all fields, call
     * {@link #readPullRequestReviewComment()}.
     *
     * @see GHPullRequest#listReviewComments()
     */
    @SuppressFBWarnings(value = { "UWF_UNWRITTEN_FIELD" }, justification = "JSON API")
    public static class ReviewComment extends GHObject {

        private GHCommentAuthorAssociation authorAssociation;
        private String body;
        private String commitId;
        private String diffHunk;
        private String htmlUrl;
        private String originalCommitId;
        private int originalPosition = -1;
        private String path;
        private int position = -1;
        private Long pullRequestReviewId = -1L;
        private String pullRequestUrl;
        private GHPullRequestReviewCommentReactions reactions;
        private GHUser user;

        GHPullRequest owner;

        /**
         * Create default ReviewComment instance
         */
        public ReviewComment() {
        }

        /**
         * Gets the author association to the project.
         *
         * @return the author association to the project
         */
        public GHCommentAuthorAssociation getAuthorAssociation() {
            return authorAssociation;
        }

        /**
         * The comment itself.
         *
         * @return the body
         */
        public String getBody() {
            return body;
        }

        /**
         * Gets commit id.
         *
         * @return the commit id
         */
        public String getCommitId() {
            return commitId;
        }

        /**
         * Gets diff hunk.
         *
         * @return the diff hunk
         */
        public String getDiffHunk() {
            return diffHunk;
        }

        /**
         * Gets the html url.
         *
         * @return the html url
         */
        public URL getHtmlUrl() {
            return GitHubClient.parseURL(htmlUrl);
        }

        /**
         * Gets commit id.
         *
         * @return the original commit id
         */
        public String getOriginalCommitId() {
            return originalCommitId;
        }

        /**
         * Gets original position.
         *
         * @return the original position
         */
        public int getOriginalPosition() {
            return originalPosition;
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

        /**
         * Gets The ID of the pull request review to which the comment belongs.
         *
         * @return {@link Long} the ID of the pull request review
         */
        public Long getPullRequestReviewId() {
            return pullRequestReviewId != null ? pullRequestReviewId : -1;
        }

        /**
         * Gets URL for the pull request that the review comment belongs to.
         *
         * @return {@link URL} the URL of the pull request
         */
        public URL getPullRequestUrl() {
            return GitHubClient.parseURL(pullRequestUrl);
        }

        /**
         * Gets the Reaction Rollup.
         *
         * @return {@link GHPullRequestReviewCommentReactions} the reaction rollup
         */
        public GHPullRequestReviewCommentReactions getReactions() {
            return reactions;
        }

        /**
         * Gets the user who posted this comment.
         *
         * @return the user
         * @throws IOException
         *             the io exception
         */
        public GHUser getUser() throws IOException {
            return owner.root().getUser(user.getLogin());
        }

        /**
         * Fetches the full {@link GHPullRequestReviewComment} from the API, which includes all fields such as
         * {@link GHPullRequestReviewComment#getLine() line}, {@link GHPullRequestReviewComment#getOriginalLine()
         * originalLine}, {@link GHPullRequestReviewComment#getSide() side}, etc.
         *
         * @return the full {@link GHPullRequestReviewComment}
         * @throws IOException
         *             if an I/O error occurs
         */
        public GHPullRequestReviewComment readPullRequestReviewComment() throws IOException {
            return owner.root()
                    .createRequest()
                    .withUrlPath("/repos/" + owner.getRepository().getFullName() + "/pulls/comments/" + getId())
                    .fetch(GHPullRequestReviewComment.class)
                    .wrapUp(owner);
        }

        /**
         * Wrap up.
         *
         * @param owner
         *            the owner
         * @return the review comment
         */
        ReviewComment wrapUp(GHPullRequest owner) {
            this.owner = owner;
            return this;
        }
    }

    private String body;

    private String commitId;

    private String htmlUrl;
    private GHPullRequestReviewState state;
    private String submittedAt;
    private GHUser user;
    /** The owner. */
    GHPullRequest owner;
    /**
     * Create default GHPullRequestReview instance
     */
    public GHPullRequestReview() {
    }

    /**
     * Deletes this review.
     *
     * @throws IOException
     *             the io exception
     */
    public void delete() throws IOException {
        owner.root().createRequest().method("DELETE").withUrlPath(getApiRoute()).send();
    }

    /**
     * Dismisses this review.
     *
     * @param message
     *            the message
     * @throws IOException
     *             the io exception
     */
    public void dismiss(String message) throws IOException {
        owner.root()
                .createRequest()
                .method("PUT")
                .with("message", message)
                .withUrlPath(getApiRoute() + "/dismissals")
                .send();
        state = GHPullRequestReviewState.DISMISSED;
    }

    /**
     * The comment itself.
     *
     * @return the body
     */
    public String getBody() {
        return body;
    }

    /**
     * Gets commit id.
     *
     * @return the commit id
     */
    public String getCommitId() {
        return commitId;
    }

    /**
     * Since this method does not exist, we forward this value.
     *
     * @return the created at
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    @WithBridgeMethods(value = Date.class, adapterMethod = "instantToDate")
    public Instant getCreatedAt() throws IOException {
        return getSubmittedAt();
    }

    /**
     * Gets the html url.
     *
     * @return the html url
     */
    public URL getHtmlUrl() {
        return GitHubClient.parseURL(htmlUrl);
    }

    /**
     * Gets the pull request to which this review is associated.
     *
     * @return the parent
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHPullRequest getParent() {
        return owner;
    }

    /**
     * Gets state.
     *
     * @return the state
     */
    @CheckForNull
    public GHPullRequestReviewState getState() {
        return state;
    }

    /**
     * When was this resource created?.
     *
     * @return the submitted at
     */
    @WithBridgeMethods(value = Date.class, adapterMethod = "instantToDate")
    public Instant getSubmittedAt() {
        return GitHubClient.parseInstant(submittedAt);
    }

    /**
     * Gets the user who posted this review.
     *
     * @return the user
     * @throws IOException
     *             the io exception
     */
    public GHUser getUser() throws IOException {
        if (user != null) {
            return owner.root().getUser(user.getLogin());
        }
        return null;
    }

    /**
     * Obtains all the review comments associated with this pull request review.
     *
     * <p>
     * The GitHub API endpoint used by this method returns a limited set of fields. To obtain full comment data
     * including line numbers, use {@link ReviewComment#readPullRequestReviewComment()} on individual comments, or use
     * {@link GHPullRequest#listReviewComments()} instead.
     *
     * @return the paged iterable of {@link ReviewComment} objects
     * @see GHPullRequest#listReviewComments()
     * @see ReviewComment#readPullRequestReviewComment()
     */
    public PagedIterable<ReviewComment> listReviewComments() {
        return owner.root()
                .createRequest()
                .withUrlPath(getApiRoute() + "/comments")
                .toIterable(ReviewComment[].class, item -> item.wrapUp(owner));
    }

    /**
     * Updates the comment.
     *
     * @param body
     *            the body
     * @param event
     *            the event
     * @throws IOException
     *             the io exception
     */
    public void submit(String body, GHPullRequestReviewEvent event) throws IOException {
        owner.root()
                .createRequest()
                .method("POST")
                .with("body", body)
                .with("event", event.action())
                .withUrlPath(getApiRoute() + "/events")
                .fetchInto(this);
        this.body = body;
        this.state = event.toState();
    }

    /**
     * Gets api route.
     *
     * @return the api route
     */
    protected String getApiRoute() {
        return owner.getApiRoute() + "/reviews/" + getId();
    }

    /**
     * Wrap up.
     *
     * @param owner
     *            the owner
     * @return the GH pull request review
     */
    GHPullRequestReview wrapUp(GHPullRequest owner) {
        this.owner = owner;
        return this;
    }
}
