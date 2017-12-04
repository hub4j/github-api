/*
 * GitHub API for Java
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.kohsuke.github;

import java.io.IOException;
import java.net.URL;

import static org.kohsuke.github.Previews.*;

/**
 * Review to the pull request
 *
 * @see GHPullRequest#listReviews()
 * @see GHPullRequest#createReview(String, GHPullRequestReviewState, GHPullRequestReviewComment...)
 */
public class GHPullRequestReview extends GHObject {
    GHPullRequest owner;

    private String body;
    private GHUser user;
    private String commit_id;
    private GHPullRequestReviewState state;

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
     * Updates the comment.
     */
    @Preview
    @Deprecated
    public void submit(String body, GHPullRequestReviewState event) throws IOException {
        new Requester(owner.root).method("POST")
                .with("body", body)
                .with("event", event.action())
                .withPreview("application/vnd.github.black-cat-preview+json")
                .to(getApiRoute()+"/events",this);
        this.body = body;
        this.state = event;
    }

    /**
     * Deletes this review.
     */
    @Preview
    @Deprecated
    public void delete() throws IOException {
        new Requester(owner.root).method("DELETE")
                .withPreview(BLACK_CAT)
                .to(getApiRoute());
    }

    /**
     * Dismisses this review.
     */
    @Preview
    @Deprecated
    public void dismiss(String message) throws IOException {
        new Requester(owner.root).method("PUT")
                .with("message", message)
                .withPreview(BLACK_CAT)
                .to(getApiRoute()+"/dismissals");
        state = GHPullRequestReviewState.DISMISSED;
    }

    /**
     * Obtains all the review comments associated with this pull request review.
     */
    @Preview
    @Deprecated
    public PagedIterable<GHPullRequestReviewComment> listReviewComments() throws IOException {
        return new PagedIterable<GHPullRequestReviewComment>() {
            public PagedIterator<GHPullRequestReviewComment> _iterator(int pageSize) {
                return new PagedIterator<GHPullRequestReviewComment>(
                        owner.root.retrieve()
                                .withPreview(BLACK_CAT)
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
