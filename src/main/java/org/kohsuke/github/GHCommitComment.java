package org.kohsuke.github;

import java.io.IOException;
import java.net.URL;
import java.util.Date;

/**
 * A comment attached to a commit (or a specific line in a specific file of a commit.)
 *
 * @author Kohsuke Kawaguchi
 * @see GHRepository#listCommitComments()
 * @see GHCommit#listComments()
 * @see GHCommit#createComment(String, String, Integer, Integer)
 */
public class GHCommitComment {
    private GHRepository owner;

    String updated_at, created_at;
    String body, url, html_url, commit_id;
    Integer line;
    int id;
    String path;
    User user;

    static class User {
        // TODO: what if someone who doesn't have an account on GitHub makes a commit?
        String url,avatar_url,login,gravatar_id;
        int id;
    }

    public GHRepository getOwner() {
        return owner;
    }

    public Date getCreatedAt() {
        return GitHub.parseDate(created_at);
    }

    public Date getUpdatedAt() {
        return GitHub.parseDate(updated_at);
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

    public int getId() {
        return id;
    }

    /**
     * Gets the user who put this comment.
     */
    public GHUser getUser() throws IOException {
        return owner.root.getUser(user.login);
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
        GHCommitComment r = new Poster(owner.root)
                .with("body",body)
                .withCredential()
                .to(getApiTail(),GHCommitComment.class,"PATCH");
        this.body = body;
    }

    /**
     * Deletes this comment.
     */
    public void delete() throws IOException {
        new Poster(owner.root).withCredential().to(getApiTail(),null,"DELETE");
    }

    private String getApiTail() {
        return String.format("/repos/%s/%s/comments/%s",owner.getOwnerName(),owner.getName(),id);
    }


    GHCommitComment wrap(GHRepository owner) {
        this.owner = owner;
        return this;
    }
}
