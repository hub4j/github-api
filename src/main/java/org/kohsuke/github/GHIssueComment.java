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
