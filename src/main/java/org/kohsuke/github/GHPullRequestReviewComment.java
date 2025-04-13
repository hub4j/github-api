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

import javax.annotation.CheckForNull;

// TODO: Auto-generated Javadoc
/**
 * Review comment to the pull request.
 *
 * @author Julien Henry
 * @see GHPullRequest#listReviewComments() GHPullRequest#listReviewComments()
 * @see GHPullRequest#createReviewComment(String, String, String, int) GHPullRequest#createReviewComment(String, String,
 *      String, int)
 */
public class GHPullRequestReviewComment extends GHObject implements Reactable {

    /**
     * Create default GHPullRequestReviewComment instance
     */
    public GHPullRequestReviewComment() {
    }

    /** The owner. */
    GHPullRequest owner;

    private Long pullRequestReviewId = -1L;
    private String body;
    private GHUser user;
    private String path;
    private String htmlUrl;
    private String pullRequestUrl;
    private int position = -1;
    private int originalPosition = -1;
    private long inReplyToId = -1L;
    private Integer startLine = -1;
    private Integer originalStartLine = -1;
    private String startSide;
    private int line = -1;
    private int originalLine = -1;
    private String side;
    private String diffHunk;
    private String commitId;
    private String originalCommitId;
    private String bodyHtml;
    private String bodyText;
    private GHPullRequestReviewCommentReactions reactions;
    private GHCommentAuthorAssociation authorAssociation;

    /**
     * Wrap up.
     *
     * @param owner
     *            the owner
     * @return the GH pull request review comment
     */
    GHPullRequestReviewComment wrapUp(GHPullRequest owner) {
        this.owner = owner;
        return this;
    }

    /**
     * Gets the pull request to which this review comment is associated.
     *
     * @return the parent
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHPullRequest getParent() {
        return owner;
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
    @CheckForNull
    public int getPosition() {
        return position;
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
     * Gets diff hunk.
     *
     * @return the diff hunk
     */
    public String getDiffHunk() {
        return diffHunk;
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
     * Gets commit id.
     *
     * @return the commit id
     */
    public String getOriginalCommitId() {
        return originalCommitId;
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
     * Gets in reply to id.
     *
     * @return the in reply to id
     */
    @CheckForNull
    public long getInReplyToId() {
        return inReplyToId;
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
     * Gets The first line of the range for a multi-line comment.
     *
     * @return the start line
     */
    public int getStartLine() {
        return startLine != null ? startLine : -1;
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
     * Gets The side of the first line of the range for a multi-line comment.
     *
     * @return {@link Side} the side of the first line
     */
    public Side getStartSide() {
        return Side.from(startSide);
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
     * Gets The line of the blob to which the comment applies. The last line of the range for a multi-line comment.
     *
     * @return the line to which the comment applies
     */
    public int getOriginalLine() {
        return originalLine;
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
     * Gets The body in html format.
     *
     * @return {@link String} the body in html format
     */
    public String getBodyHtml() {
        return bodyHtml;
    }

    /**
     * Gets The body text.
     *
     * @return {@link String} the body text
     */
    public String getBodyText() {
        return bodyText;
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
     * The side of the diff to which the comment applies
     */
    public static enum Side {
        /** Right side */
        RIGHT,
        /** Left side */
        LEFT,
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

    /**
     * Updates the comment.
     *
     * @param body
     *            the body
     * @throws IOException
     *             the io exception
     */
    public void update(String body) throws IOException {
        owner.root().createRequest().method("PATCH").with("body", body).withUrlPath(getApiRoute()).fetchInto(this);
        this.body = body;
    }

    /**
     * Deletes this review comment.
     *
     * @throws IOException
     *             the io exception
     */
    public void delete() throws IOException {
        owner.root().createRequest().method("DELETE").withUrlPath(getApiRoute()).send();
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
                .wrapUp(owner);
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
    public GHReaction createReaction(ReactionContent content) throws IOException {
        return owner.root()
                .createRequest()
                .method("POST")
                .with("content", content.getContent())
                .withUrlPath(getApiRoute() + "/reactions")
                .fetch(GHReaction.class);
    }

    /**
     * Delete reaction.
     *
     * @param reaction
     *            the reaction
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void deleteReaction(GHReaction reaction) throws IOException {
        owner.root()
                .createRequest()
                .method("DELETE")
                .withUrlPath(getApiRoute(), "reactions", String.valueOf(reaction.getId()))
                .send();
    }

    /**
     * List reactions.
     *
     * @return the paged iterable
     */
    public PagedIterable<GHReaction> listReactions() {
        return owner.root()
                .createRequest()
                .withUrlPath(getApiRoute() + "/reactions")
                .toIterable(GHReaction[].class, item -> owner.root());
    }
}
