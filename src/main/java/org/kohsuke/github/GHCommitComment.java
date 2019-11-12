package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.net.URL;

import static org.kohsuke.github.Previews.*;

/**
 * A comment attached to a commit (or a specific line in a specific file of a commit.)
 *
 * @author Kohsuke Kawaguchi
 * @see GHRepository#listCommitComments()
 * @see GHCommit#listComments()
 * @see GHCommit#createComment(String, String, Integer, Integer)
 */
@SuppressFBWarnings(value = {"UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", 
    "NP_UNWRITTEN_FIELD"}, justification = "JSON API")
public class GHCommitComment extends GHObject implements Reactable {
    private GHRepository owner;

    String body, html_url, commit_id;
    Integer line;
    String path;
    GHUser user;  // not fully populated. beware.

    public GHRepository getOwner() {
        return owner;
    }

    /**
     * URL like 'https://github.com/kohsuke/sandbox-ant/commit/8ae38db0ea5837313ab5f39d43a6f73de3bd9000#commitcomment-1252827' to
     * show this commit comment in a browser.
     */
    public URL getHtmlUrl() {
        return GitHub.parseURL(html_url);
    }

    public String getSHA1() {
        return commit_id;
    }

    /**
     * Commit comment in the GitHub flavored markdown format.
     */
    public String getBody() {
        return body;
    }

    /**
     * A commit comment can be on a specific line of a specific file, if so, this field points to a file.
     * Otherwise null.
     */
    public String getPath() {
        return path;
    }

    /**
     * A commit comment can be on a specific line of a specific file, if so, this field points to the line number in the file.
     * Otherwise -1.
     */
    public int getLine() {
        return line!=null ? line : -1;
    }

    /**
     * Gets the user who put this comment.
     */
    public GHUser getUser() throws IOException {
        return owner == null || owner.getRoot().isOffline() ? user : owner.getRoot().getUser(user.login);
    }

    /**
     * Gets the commit to which this comment is associated with.
     */
    public GHCommit getCommit() throws IOException {
        return getOwner().getCommit(getSHA1());
    }

    /**
     * Updates the body of the commit message.
     */
    public void update(String body) throws IOException {
        owner.createRequester()
            .with("body", body)
                .method("PATCH").to(getApiTail(), GHCommitComment.class);
        this.body = body;
    }

    @Preview @Deprecated
    public GHReaction createReaction(ReactionContent content) throws IOException {
        return owner.createRequester()
            .withPreview(SQUIRREL_GIRL)
                .with("content", content.getContent())
                .to(getApiTail()+"/reactions", GHReaction.class).wrap(owner.getRoot());
    }

    @Preview @Deprecated
    public PagedIterable<GHReaction> listReactions() {
        return owner.createRequester().method("GET").withPreview(SQUIRREL_GIRL)
            .asPagedIterable(
                getApiTail()+"/reactions",
                GHReaction[].class,
                item -> item.wrap(owner.getRoot()) );
    }

    /**
     * Deletes this comment.
     */
    public void delete() throws IOException {
        owner.createRequester().method("DELETE").to(getApiTail());
    }

    private String getApiTail() {
        return String.format("/repos/%s/%s/comments/%s",owner.getOwnerName(),owner.getName(),id);
    }


    GHCommitComment wrap(GHRepository owner) {
        this.owner = owner;
        if (owner.getRoot().isOffline()) {
            user.wrapUp(owner.getRoot());
        }
        return this;
    }
}
