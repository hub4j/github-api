/*
 * The MIT License
 *
 * Copyright (c) 2011, Eric Maupin
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

import java.io.IOException;
import java.net.URL;

@SuppressFBWarnings(value = {"UWF_UNWRITTEN_FIELD"}, justification = "JSON API")
public abstract class GHPullRequestReviewAbstract extends GHObject {
    protected GHPullRequest owner;

    protected String body;
    private GHUser user;
    private String commit_id;

    public abstract GHPullRequestReviewState getState();

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

    @Override
    public URL getHtmlUrl() {
        return null;
    }

    String getApiRoute() {
        return owner.getApiRoute() + "/reviews/" + id;
    }

    /**
     * Deletes this review.
     */
    public void delete() throws IOException {
        new Requester(owner.root).method("DELETE")
                .to(getApiRoute());
    }

    /**
     * Obtains all the review comments associated with this pull request review.
     */
    public PagedIterable<GHPullRequestReviewComment> listReviewComments() {
        return new PagedIterable<GHPullRequestReviewComment>() {
            public PagedIterator<GHPullRequestReviewComment> _iterator(int pageSize) {
                return new PagedIterator<GHPullRequestReviewComment>(
                        owner.root.retrieve()
                                .asIterator(getApiRoute() + "/comments",
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
