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
 * Comment to the issue
 *
 * @author Kohsuke Kawaguchi
 */
public class GHIssueComment extends GHObject implements Reactable {
    GHIssue owner;

    private String body, gravatar_id;
    private GHUser user; // not fully populated. beware.

    /*package*/ GHIssueComment wrapUp(GHIssue owner) {
        this.owner = owner;
        return this;
    }

    /**
     * Gets the issue to which this comment is associated.
     */
    public GHIssue getParent() {
        return owner;
    }

    /**
     * The comment itself.
     */
    public String getBody() {
        return body;
    }

    /**
     * Gets the ID of the user who posted this comment.
     */
    @Deprecated
    public String getUserName() {
        return user.getLogin();
    }

    /**
     * Gets the user who posted this comment.
     */
    public GHUser getUser() throws IOException {
        return owner == null || owner.root.isOffline() ? user : owner.root.getUser(user.getLogin());
    }
    
    /**
     * @deprecated This object has no HTML URL.
     */
    @Override
    public URL getHtmlUrl() {
        return null;
    }
    
    /**
     * Updates the body of the issue comment.
     */
    public void update(String body) throws IOException {
        new Requester(owner.root).with("body", body).method("PATCH").to(getApiRoute(), GHIssueComment.class);
        this.body = body;
    }

    /**
     * Deletes this issue comment.
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

    private String getApiRoute() {
        return "/repos/"+owner.getRepository().getOwnerName()+"/"+owner.getRepository().getName()+"/issues/comments/" + id;
    }
}
