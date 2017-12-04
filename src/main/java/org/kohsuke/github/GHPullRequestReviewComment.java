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
 * Review comment to the pull request
 *
 * @author Julien Henry
 * @see GHPullRequest#listReviewComments()
 * @see GHPullRequest#createReviewComment(String, String, String, int)
 */
public class GHPullRequestReviewComment extends GHObject implements Reactable {
    GHPullRequest owner;

    private String body;
    private GHUser user;
    private String path;
    private int position;
    private int originalPosition;

    public static GHPullRequestReviewComment draft(String body, String path, int position) {
        GHPullRequestReviewComment result = new GHPullRequestReviewComment();
        result.body = body;
        result.path = path;
        result.position = position;
        return result;
    }

    /*package*/ GHPullRequestReviewComment wrapUp(GHPullRequest owner) {
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

    public String getPath() {
        return path;
    }

    public int getPosition() {
        return position;
    }

    public int getOriginalPosition() {
        return originalPosition;
    }

    @Override
    public URL getHtmlUrl() {
        return null;
    }

    protected String getApiRoute() {
        return "/repos/"+owner.getRepository().getFullName()+"/pulls/comments/"+id;
    }

    /**
     * Updates the comment.
     */
    public void update(String body) throws IOException {
        new Requester(owner.root).method("PATCH").with("body", body).to(getApiRoute(),this);
        this.body = body;
    }

    /**
     * Deletes this review comment.
     */
    public void delete() throws IOException {
        new Requester(owner.root).method("DELETE").to(getApiRoute());
    }

    @Preview @Deprecated
    public GHReaction createReaction(ReactionContent content) throws IOException {
        return new Requester(owner.root)
                .withPreview(SQUIRREL_GIRL)
                .with("content", content.getContent())
                .to(getApiRoute()+"/reactions", GHReaction.class).wrap(owner.root);
    }

    @Preview @Deprecated
    public PagedIterable<GHReaction> listReactions() {
        return new PagedIterable<GHReaction>() {
            public PagedIterator<GHReaction> _iterator(int pageSize) {
                return new PagedIterator<GHReaction>(owner.root.retrieve().withPreview(SQUIRREL_GIRL).asIterator(getApiRoute()+"/reactions", GHReaction[].class, pageSize)) {
                    @Override
                    protected void wrapUp(GHReaction[] page) {
                        for (GHReaction c : page)
                            c.wrap(owner.root);
                    }
                };
            }
        };
    }
}
