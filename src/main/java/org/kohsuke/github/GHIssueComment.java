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

import java.io.IOException;
import java.net.URL;

import static org.kohsuke.github.internal.Previews.SQUIRREL_GIRL;

// TODO: Auto-generated Javadoc
/**
 * Comment to the issue.
 *
 * @author Kohsuke Kawaguchi
 * @see GHIssue#comment(String) GHIssue#comment(String)
 * @see GHIssue#listComments() GHIssue#listComments()
 */
public class GHIssueComment extends GHObject implements Reactable {
    
    /** The owner. */
    GHIssue owner;

    private String body, gravatar_id, html_url, author_association;
    private GHUser user; // not fully populated. beware.

    /**
     * Wrap up.
     *
     * @param owner the owner
     * @return the GH issue comment
     */
    GHIssueComment wrapUp(GHIssue owner) {
        this.owner = owner;
        return this;
    }

    /**
     * Gets the issue to which this comment is associated.
     *
     * @return the parent
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHIssue getParent() {
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
     * Gets the ID of the user who posted this comment.
     *
     * @return the user name
     */
    @Deprecated
    public String getUserName() {
        return user.getLogin();
    }

    /**
     * Gets the user who posted this comment.
     *
     * @return the user
     * @throws IOException
     *             the io exception
     */
    public GHUser getUser() throws IOException {
        return owner == null || owner.isOffline() ? user : owner.root().getUser(user.getLogin());
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
     * Gets author association.
     *
     * @return the author association
     */
    public GHCommentAuthorAssociation getAuthorAssociation() {
        return GHCommentAuthorAssociation.valueOf(author_association);
    }

    /**
     * Updates the body of the issue comment.
     *
     * @param body
     *            the body
     * @throws IOException
     *             the io exception
     */
    public void update(String body) throws IOException {
        owner.root()
                .createRequest()
                .method("PATCH")
                .with("body", body)
                .withUrlPath(getApiRoute())
                .fetch(GHIssueComment.class);
        this.body = body;
    }

    /**
     * Deletes this issue comment.
     *
     * @throws IOException
     *             the io exception
     */
    public void delete() throws IOException {
        owner.root().createRequest().method("DELETE").withUrlPath(getApiRoute()).send();
    }

    /**
     * Creates the reaction.
     *
     * @param content the content
     * @return the GH reaction
     * @throws IOException Signals that an I/O exception has occurred.
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
     * @param reaction the reaction
     * @throws IOException Signals that an I/O exception has occurred.
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

    private String getApiRoute() {
        return "/repos/" + owner.getRepository().getOwnerName() + "/" + owner.getRepository().getName()
                + "/issues/comments/" + getId();
    }
}
