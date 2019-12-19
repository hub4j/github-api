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

import java.io.IOException;
import java.net.URL;

import javax.annotation.CheckForNull;

import static org.kohsuke.github.Previews.*;

/**
 * Review comment to the pull request
 *
 * @author Julien Henry
 * @see GHPullRequest#listReviewComments() GHPullRequest#listReviewComments()
 * @see GHPullRequest#createReviewComment(String, String, String, int) GHPullRequest#createReviewComment(String, String,
 *      String, int)
 */
public class GHPullRequestReviewComment extends GHObject implements Reactable {
    GHPullRequest owner;

    private String body;
    private GHUser user;
    private String path;
    private int position = -1;
    private int original_position = -1;
    private long in_reply_to_id = -1L;

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
    public static GHPullRequestReviewComment draft(String body, String path, int position) {
        GHPullRequestReviewComment result = new GHPullRequestReviewComment();
        result.body = body;
        result.path = path;
        result.position = position;
        return result;
    }

    GHPullRequestReviewComment wrapUp(GHPullRequest owner) {
        this.owner = owner;
        return this;
    }

    /**
     * Gets the pull request to which this review comment is associated.
     *
     * @return the parent
     */
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
        return owner.root.getUser(user.getLogin());
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
     * Gets in reply to id.
     *
     * @return the in reply to id
     */
    @CheckForNull
    public long getInReplyToId() {
        return in_reply_to_id;
    }

    @Override
    public URL getHtmlUrl() {
        return null;
    }

    /**
     * Gets api route.
     *
     * @return the api route
     */
    protected String getApiRoute() {
        return "/repos/" + owner.getRepository().getFullName() + "/pulls/comments/" + id;
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
        owner.root.createRequest().method("PATCH").with("body", body).withUrlPath(getApiRoute()).fetchInto(this);
        this.body = body;
    }

    /**
     * Deletes this review comment.
     *
     * @throws IOException
     *             the io exception
     */
    public void delete() throws IOException {
        owner.root.createRequest().method("DELETE").withUrlPath(getApiRoute()).send();
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
        return owner.root.createRequest()
                .method("POST")
                .with("body", body)
                .with("in_reply_to", getId())
                .withUrlPath(getApiRoute() + "/comments")
                .fetch(GHPullRequestReviewComment.class)
                .wrapUp(owner);
    }

    @Preview
    @Deprecated
    public GHReaction createReaction(ReactionContent content) throws IOException {
        return owner.root.createRequest()
                .method("POST")
                .withPreview(SQUIRREL_GIRL)
                .with("content", content.getContent())
                .withUrlPath(getApiRoute() + "/reactions")
                .fetch(GHReaction.class)
                .wrap(owner.root);
    }

    @Preview
    @Deprecated
    public PagedIterable<GHReaction> listReactions() {
        return owner.root.createRequest()
                .withPreview(SQUIRREL_GIRL)
                .asPagedIterable(getApiRoute() + "/reactions", GHReaction[].class, item -> item.wrap(owner.root));
    }
}
