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

import static org.kohsuke.github.internal.Previews.SQUIRREL_GIRL;

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

    /** The owner. */
    GHPullRequest owner;

    private Long pull_request_review_id = -1L;
    private String body;
    private GHUser user;
    private String path;
    private String html_url;
    private String pull_request_url;
    private int position = -1;
    private int original_position = -1;
    private long in_reply_to_id = -1L;
    private Integer start_line = -1;
    private Integer original_start_line = -1;
    private String start_side;
    private int line = -1;
    private int original_line = -1;
    private String side;
    private String diff_hunk;
    private String commit_id;
    private String original_commit_id;
    private String body_html;
    private String body_text;
    private GHPullRequestReviewCommentReactions reactions;
    private GHCommentAuthorAssociation author_association;

    /**
     * Draft gh pull request review comment.
     *
     * @param body
     *            the body
     * @param path
     *            the path
     * @param position
     *            the position
     * @return the gh pull request review comment
     * @deprecated You should be using {@link GHPullRequestReviewBuilder#comment(String, String, int)}
     */
    @Deprecated
    public static GHPullRequestReviewComment draft(String body, String path, int position) {
        GHPullRequestReviewComment result = new GHPullRequestReviewComment();
        result.body = body;
        result.path = path;
        result.position = position;
        return result;
    }

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
        return original_position;
    }

    /**
     * Gets diff hunk.
     *
     * @return the diff hunk
     */
    public String getDiffHunk() {
        return diff_hunk;
    }

    /**
     * Gets commit id.
     *
     * @return the commit id
     */
    public String getCommitId() {
        return commit_id;
    }

    /**
     * Gets commit id.
     *
     * @return the commit id
     */
    public String getOriginalCommitId() {
        return original_commit_id;
    }

    /**
     * Gets the author association to the project.
     *
     * @return the author association to the project
     */
    public GHCommentAuthorAssociation getAuthorAssociation() {
        return author_association;
    }

    /**
     * Gets in reply to id.
     *
     * @return the in reply to id
     */
    @CheckForNull
    public long getInReplyToId() {
        return in_reply_to_id;
    }

    /**
     * Gets the html url.
     *
     * @return the html url
     */
    @Override
    public URL getHtmlUrl() {
        return GitHubClient.parseURL(html_url);
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
        return start_line != null ? start_line : -1;
    }

    /**
     * Gets The first line of the range for a multi-line comment.
     *
     * @return the original start line
     */
    public int getOriginalStartLine() {
        return original_start_line != null ? original_start_line : -1;
    }

    /**
     * Gets The side of the first line of the range for a multi-line comment.
     *
     * @return {@link Side} the side of the first line
     */
    public Side getStartSide() {
        return Side.from(start_side);
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
        return original_line;
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
        return pull_request_review_id != null ? pull_request_review_id : -1;
    }

    /**
     * Gets URL for the pull request that the review comment belongs to.
     *
     * @return {@link URL} the URL of the pull request
     */
    public URL getPullRequestUrl() {
        return GitHubClient.parseURL(pull_request_url);
    }

    /**
     * Gets The body in html format.
     *
     * @return {@link String} the body in html format
     */
    public String getBodyHtml() {
        return body_html;
    }

    /**
     * Gets The body text.
     *
     * @return {@link String} the body text
     */
    public String getBodyText() {
        return body_text;
    }

    /**
     * Gets the Reaction Rollup
     *
     * @return {@link GHPullRequestReviewCommentReactions} the reaction rollup
     */
    public GHPullRequestReviewCommentReactions getReactions() {
        return reactions;
    }

    public static enum Side {
        RIGHT, LEFT, UNKNOWN;

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
    @Preview(SQUIRREL_GIRL)
    public GHReaction createReaction(ReactionContent content) throws IOException {
        return owner.root()
                .createRequest()
                .method("POST")
                .withPreview(SQUIRREL_GIRL)
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
    @Preview(SQUIRREL_GIRL)
    public PagedIterable<GHReaction> listReactions() {
        return owner.root()
                .createRequest()
                .withPreview(SQUIRREL_GIRL)
                .withUrlPath(getApiRoute() + "/reactions")
                .toIterable(GHReaction[].class, item -> owner.root());
    }
}
