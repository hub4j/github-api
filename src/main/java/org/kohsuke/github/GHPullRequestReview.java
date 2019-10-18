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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Date;
import javax.annotation.CheckForNull;
import java.io.IOException;
import java.net.URL;

/**
 * Review to a pull request.
 *
 * @see GHPullRequest#listReviews()
 * @see GHPullRequestReviewBuilder
 */
@SuppressFBWarnings(value = {"UWF_UNWRITTEN_FIELD"}, justification = "JSON API")
public class GHPullRequestReview extends GHObject {
    GHPullRequest owner;

    private String body;
    private GHUser user;
    private String commit_id;
    private GHPullRequestReviewState state;
    private String submitted_at;

    /*package*/ GHPullRequestReview wrapUp(GHPullRequest owner) {
        this.owner = owner;
        return this;
    }

    /**
     * Gets the pull request to which this review is associated.
     */
    public GHPullRequest getParent() {
        return owner;
    }

    /**
     * The comment itself.
     */
    public String getBody() {
        return body;
    }

    /**
     * Gets the user who posted this review.
     */
    public GHUser getUser() throws IOException {
        return owner.root.getUser(user.getLogin());
    }

    public String getCommitId() {
        return commit_id;
    }

    @CheckForNull
    public GHPullRequestReviewState getState() {
        return state;
    }

    @Override
    public URL getHtmlUrl() {
        return null;
    }

    protected String getApiRoute() {
        return owner.getApiRoute()+"/reviews/"+id;
    }

    /**
     * When was this resource created?
     */
    public Date getSubmittedAt() throws IOException {
        return GitHub.parseDate(submitted_at);
    }

    /**
     * Since this method does not exist, we forward this value.
     */
    @Override
    public Date getCreatedAt() throws IOException {
        return getSubmittedAt();
    }

    /**
     * @deprecated
     *      Former preview method that changed when it got public. Left here for backward compatibility.
     *      Use {@link #submit(String, GHPullRequestReviewEvent)}
     */
    public void submit(String body, GHPullRequestReviewState state) throws IOException {
        submit(body,state.toEvent());
    }

    /**
     * Updates the comment.
     */
    public void submit(String body, GHPullRequestReviewEvent event) throws IOException {
        new Requester(owner.root).method("POST")
                .with("body", body)
                .with("event", event.action())
                .to(getApiRoute()+"/events",this);
        this.body = body;
        this.state = event.toState();
    }

    /**
     * Deletes this review.
     */
    public void delete() throws IOException {
        new Requester(owner.root).method("DELETE")
                .to(getApiRoute());
    }

    /**
     * Dismisses this review.
     */
    public void dismiss(String message) throws IOException {
        new Requester(owner.root).method("PUT")
                .with("message", message)
                .to(getApiRoute()+"/dismissals");
        state = GHPullRequestReviewState.DISMISSED;
    }

    /**
     * Obtains all the review comments associated with this pull request review.
     */
    public PagedIterable<GHPullRequestReviewComment> listReviewComments() throws IOException {
        return owner.root.retrieve()
            .asPagedIterable(
                getApiRoute() + "/comments",
                GHPullRequestReviewComment[].class,
                item -> item.wrapUp(owner) );
    }
}
