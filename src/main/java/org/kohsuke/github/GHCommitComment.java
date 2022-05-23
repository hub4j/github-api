package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.net.URL;

import static org.kohsuke.github.internal.Previews.SQUIRREL_GIRL;

/**
 * A comment attached to a commit (or a specific line in a specific file of a commit.)
 *
 * @author Kohsuke Kawaguchi
 * @see GHRepository#listCommitComments() GHRepository#listCommitComments()
 * @see GHCommit#listComments() GHCommit#listComments()
 * @see GHCommit#createComment(String, String, Integer, Integer) GHCommit#createComment(String, String, Integer,
 *      Integer)
 */
@SuppressFBWarnings(value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD" },
        justification = "JSON API")
public class GHCommitComment extends GHObject implements Reactable {
    private GHRepository owner;

    String body, html_url, commit_id;
    Integer line;
    String path;
    GHUser user; // not fully populated. beware.

    /**
     * Gets owner.
     *
     * @return the owner
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHRepository getOwner() {
        return owner;
    }

    /**
     * URL like
     * 'https://github.com/kohsuke/sandbox-ant/commit/8ae38db0ea5837313ab5f39d43a6f73de3bd9000#commitcomment-1252827' to
     * show this commit comment in a browser.
     */
    public URL getHtmlUrl() {
        return GitHubClient.parseURL(html_url);
    }

    /**
     * Gets sha 1.
     *
     * @return the sha 1
     */
    public String getSHA1() {
        return commit_id;
    }

    /**
     * Commit comment in the GitHub flavored markdown format.
     *
     * @return the body
     */
    public String getBody() {
        return body;
    }

    /**
     * A commit comment can be on a specific line of a specific file, if so, this field points to a file. Otherwise
     * null.
     *
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * A commit comment can be on a specific line of a specific file, if so, this field points to the line number in the
     * file. Otherwise -1.
     *
     * @return the line
     */
    public int getLine() {
        return line != null ? line : -1;
    }

    /**
     * Gets the user who put this comment.
     *
     * @return the user
     * @throws IOException
     *             the io exception
     */
    public GHUser getUser() throws IOException {
        return owner == null || owner.isOffline() ? user : owner.root().getUser(user.login);
    }

    /**
     * Gets the commit to which this comment is associated with.
     *
     * @return the commit
     * @throws IOException
     *             the io exception
     */
    public GHCommit getCommit() throws IOException {
        return getOwner().getCommit(getSHA1());
    }

    /**
     * Updates the body of the commit message.
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
                .withUrlPath(getApiTail())
                .fetch(GHCommitComment.class);
        this.body = body;
    }

    @Preview(SQUIRREL_GIRL)
    public GHReaction createReaction(ReactionContent content) throws IOException {
        return owner.root()
                .createRequest()
                .method("POST")
                .withPreview(SQUIRREL_GIRL)
                .with("content", content.getContent())
                .withUrlPath(getApiTail() + "/reactions")
                .fetch(GHReaction.class);
    }

    public void deleteReaction(GHReaction reaction) throws IOException {
        owner.root()
                .createRequest()
                .method("DELETE")
                .withUrlPath(getApiTail(), "reactions", String.valueOf(reaction.getId()))
                .send();
    }

    @Preview(SQUIRREL_GIRL)
    public PagedIterable<GHReaction> listReactions() {
        return owner.root()
                .createRequest()
                .withPreview(SQUIRREL_GIRL)
                .withUrlPath(getApiTail() + "/reactions")
                .toIterable(GHReaction[].class, item -> owner.root());
    }

    /**
     * Deletes this comment.
     *
     * @throws IOException
     *             the io exception
     */
    public void delete() throws IOException {
        owner.root().createRequest().method("DELETE").withUrlPath(getApiTail()).send();
    }

    private String getApiTail() {
        return String.format("/repos/%s/%s/comments/%s", owner.getOwnerName(), owner.getName(), getId());
    }

    GHCommitComment wrap(GHRepository owner) {
        this.owner = owner;
        return this;
    }
}
