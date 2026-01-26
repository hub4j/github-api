/*
 * The MIT License
 *
 * Copyright (c) 2010, Kohsuke Kawaguchi
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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.kohsuke.github.internal.EnumUtils;

import java.io.IOException;
import java.net.URL;

// TODO: Auto-generated Javadoc
/**
 * Review comment to the pull request.
 *
 * @author Julien Henry
 * @see GHPullRequest#listReviewComments() GHPullRequest#listReviewComments()
 * @see GHPullRequest#createReviewComment(String, String, String, int) GHPullRequest#createReviewComment(String, String,
 *      String, int)
 */
public class GHPullRequestReviewComment extends GHIssueComment implements Refreshable {

    /**
     * The side of the diff to which the comment applies.
     *
     * @see <a href="https://docs.github.com/en/rest/pulls/comments">Pull Request Review Comments API</a>
     */
    public static enum Side {
        /** Left side */
        LEFT,
        /** Right side */
        RIGHT,
        /** Unknown side */
        UNKNOWN;

        /**
         * From.
         *
         * @param value
         *            the value
         * @return the status
         */
        public static Side from(String value) {
            return EnumUtils.getEnumOrDefault(Side.class, value, Side.UNKNOWN);
        }

    }

    // PR review comment specific fields (not in GHIssueComment)
    private String commitId;
    private String diffHunk;
    private long inReplyToId = -1L;
    private int line = -1;
    private String originalCommitId;
    private int originalLine = -1;
    private int originalPosition = -1;
    private Integer originalStartLine;
    private String path;
    private int position = -1;
    private Long pullRequestReviewId;
    private String pullRequestUrl;
    private GHPullRequestReviewCommentReactions reactions;
    private String side;
    private Integer startLine;
    private String startSide;

    /**
     * Create default GHPullRequestReviewComment instance
     */
    public GHPullRequestReviewComment() {
    }

    /**
     * Creates the reaction.
     *
     * @param content
     *            the content
     * @return the GH reaction
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public GHReaction createReaction(ReactionContent content) throws IOException {
        return owner.root()
                .createRequest()
                .method("POST")
                .with("content", content.getContent())
                .withUrlPath(getApiRoute() + "/reactions")
                .fetch(GHReaction.class);
    }

    /**
     * Deletes this review comment.
     *
     * @throws IOException
     *             the io exception
     */
    @Override
    public void delete() throws IOException {
        owner.root().createRequest().method("DELETE").withUrlPath(getApiRoute()).send();
    }

    /**
     * Delete reaction.
     *
     * @param reaction
     *            the reaction
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void deleteReaction(GHReaction reaction) throws IOException {
        owner.root()
                .createRequest()
                .method("DELETE")
                .withUrlPath(getApiRoute(), "reactions", String.valueOf(reaction.getId()))
                .send();
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
     * Gets in reply to id.
     *
     * @return the in reply to id, or -1 if not a reply
     */
    public long getInReplyToId() {
        return inReplyToId;
    }

    /**
     * Gets The line of the blob to which the comment applies. The last line of the range for a multi-line comment.
     *
     * @return the line to which the comment applies
     */
    public int getLine() {
        return line;
    }

    /**
     * Gets commit id.
     *
     * @return the commit id
     */
    public String getOriginalCommitId() {
        return originalCommitId;
    }

    /**
     * Gets The line of the blob to which the comment applies. The last line of the range for a multi-line comment.
     *
     * @return the line to which the comment applies
     */
    public int getOriginalLine() {
        return originalLine;
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
     * Gets The first line of the range for a multi-line comment.
     *
     * @return the original start line
     */
    public int getOriginalStartLine() {
        return originalStartLine != null ? originalStartLine : -1;
    }

    /**
     * Gets the pull request to which this review comment is associated.
     *
     * @return the parent pull request
     */
    @Override
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHPullRequest getParent() {
        return (GHPullRequest) owner;
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
     * @return the position, or -1 if not available
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
     * Gets the Reaction Rollup
     *
     * @return {@link GHPullRequestReviewCommentReactions} the reaction rollup
     */
    public GHPullRequestReviewCommentReactions getReactions() {
        return reactions;
    }

    /**
     * Gets The side of the diff to which the comment applies. The side of the last line of the range for a multi-line
     * comment
     *
     * @return {@link Side} the side if the diff to which the comment applies
     */
    public Side getSide() {
        return Side.from(side);
    }

    /**
     * Gets The first line of the range for a multi-line comment.
     *
     * @return the start line
     */
    public int getStartLine() {
        return startLine != null ? startLine : -1;
    }

    /**
     * Gets The side of the first line of the range for a multi-line comment.
     *
     * @return {@link Side} the side of the first line
     */
    public Side getStartSide() {
        return Side.from(startSide);
    }

    /**
     * Gets the user who posted this comment.
     *
     * @return the user
     * @throws IOException
     *             the io exception
     */
    @Override
    public GHUser getUser() throws IOException {
        return owner.root().getUser(user.getLogin());
    }

    /**
     * List reactions.
     *
     * @return the paged iterable
     */
    @Override
    public PagedIterable<GHReaction> listReactions() {
        return owner.root()
                .createRequest()
                .withUrlPath(getApiRoute() + "/reactions")
                .toIterable(GHReaction[].class, item -> owner.root());
    }

    /**
     * Refreshes this comment by fetching the full data from the API.
     *
     * <p>
     * This is useful when the comment was obtained via {@link GHPullRequestReview#listReviewComments()}, which uses a
     * GitHub API endpoint that does not return all fields. After calling this method, fields like {@link #getLine()},
     * {@link #getOriginalLine()}, {@link #getSide()}, etc. will return their actual values.
     *
     * @throws IOException
     *             if an I/O error occurs
     * @see GHPullRequest#listReviewComments()
     */
    @Override
    public void refresh() throws IOException {
        owner.root().createRequest().withUrlPath(getApiRoute()).fetchInto(this).wrapUp(getParent());
    }

    /**
     * Create a new comment that replies to this comment.
     *
     * @param body
     *            the body
     * @return the gh pull request review comment
     * @throws IOException
     *             the io exception
     */
    public GHPullRequestReviewComment reply(String body) throws IOException {
        return owner.root()
                .createRequest()
                .method("POST")
                .with("body", body)
                .withUrlPath(getApiRoute(true) + "/replies")
                .fetch(GHPullRequestReviewComment.class)
                .wrapUp(getParent());
    }

    /**
     * Updates the comment.
     *
     * @param body
     *            the body
     * @throws IOException
     *             the io exception
     */
    @Override
    public void update(String body) throws IOException {
        owner.root().createRequest().method("PATCH").with("body", body).withUrlPath(getApiRoute()).fetchInto(this);
        this.body = body;
    }

    /**
     * Gets api route.
     *
     * @return the api route
     */
    protected String getApiRoute() {
        return getApiRoute(false);
    }

    /**
     * Gets api route.
     *
     * @param includePullNumber
     *            if true, includes the owning pull request's number in the route.
     *
     * @return the api route
     */
    protected String getApiRoute(boolean includePullNumber) {
        return "/repos/" + owner.getRepository().getFullName() + "/pulls"
                + (includePullNumber ? "/" + owner.getNumber() : "") + "/comments/" + getId();
    }

    /**
     * Wrap up.
     *
     * @param pullRequest
     *            the pull request owner
     * @return the GH pull request review comment
     */
    GHPullRequestReviewComment wrapUp(GHPullRequest pullRequest) {
        this.owner = pullRequest;
        return this;
    }
}
