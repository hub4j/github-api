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
import java.util.List;

/**
 * Review to the pull request
 *
 * @author Paulo Miguel Almeida
 * @see GHPullRequest#listReviews()
 * @see GHPullRequest#createReview(String, String, GHPullRequestReviewEventType)
 * @see GHPullRequest#createReview(String, String, GHPullRequestReviewEventType, List)
 */
public class GHPullRequestReview extends GHObject {
    GHPullRequest owner;

    private String body;
    private GHUser user;
    private String commitId;
    private String state;

    /*package*/ GHPullRequestReview wrapUp(GHPullRequest owner) {
        this.owner = owner;
        return this;
    }

    /**
     * Gets the pull request to which this review comment is associated.
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
     * Gets the user who posted this comment.
     */
    public GHUser getUser() throws IOException {
        return owner.root.getUser(user.getLogin());
    }

    public String getCommitId(){
        return commitId;
    }

    public String getState(){
        return state;
    }

    @Override
    public URL getHtmlUrl() {
        return null;
    }

    protected String getApiRoute() {
        return "/repos/" + owner.getRepository().getFullName() + "/pulls/" + owner.getNumber() + "/reviews/" + id;
    }

    public void submit(String body, GHPullRequestReviewEventType event) throws IOException{
        new Requester(owner.root).method("POST")
                .with("body", body)
                ._with("event", event)
                .to(getApiRoute() + "/events");
    }

    public void delete() throws IOException{
        new Requester(owner.root).method("DELETE").to(getApiRoute());
    }

    /**
     * Obtains all the review comments associated with this pull request review.
     */
    public PagedIterable<GHPullRequestReviewComment> listComments() throws IOException {
        return new PagedIterable<GHPullRequestReviewComment>() {
            public PagedIterator<GHPullRequestReviewComment> _iterator(int pageSize) {
                return new PagedIterator<GHPullRequestReviewComment>(owner.root.retrieve().asIterator(getApiRoute() + "/comments",
                        GHPullRequestReviewComment[].class, pageSize)) {
                    protected void wrapUp(GHPullRequestReviewComment[] page) {
                        for (GHPullRequestReviewComment c : page)
                            c.wrapUp(owner);
                    }
                };
            }
        };
    }
}
