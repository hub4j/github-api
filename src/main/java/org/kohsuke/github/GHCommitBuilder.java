package org.kohsuke.github;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * Builder pattern for creating a new commit. Based on https://developer.github.com/v3/git/commits/#create-a-commit
 */
public class GHCommitBuilder {
    private static final class UserInfo {
        private final String date;
        private final String email;
        private final String name;

        private UserInfo(String name, String email, Instant date) {
            this.name = name;
            this.email = email;
            this.date = GitHubClient.printInstant(date);
        }
    }
    private final List<String> parents = new ArrayList<String>();

    private final GHRepository repo;

    private final Requester req;

    /**
     * Instantiates a new GH commit builder.
     *
     * @param repo
     *            the repo
     */
    GHCommitBuilder(GHRepository repo) {
        this.repo = repo;
        req = repo.root().createRequest().method("POST");
    }

    /**
     * Configures the author of this commit.
     *
     * @param name
     *            the name
     * @param email
     *            the email
     * @param date
     *            the date
     * @return the gh commit builder
     * @deprecated use {@link #author(String, String, Instant)} instead
     */
    @Deprecated
    public GHCommitBuilder author(String name, String email, Date date) {
        return author(name, email, GitHubClient.toInstantOrNull(date));
    }

    /**
     * Configures the author of this commit.
     *
     * @param name
     *            the name
     * @param email
     *            the email
     * @param date
     *            the date
     * @return the gh commit builder
     */
    public GHCommitBuilder author(String name, String email, Instant date) {
        req.with("author", new UserInfo(name, email, date));
        return this;
    }

    /**
     * Configures the committer of this commit.
     *
     * @param name
     *            the name
     * @param email
     *            the email
     * @param date
     *            the date
     * @return the gh commit builder
     * @deprecated use {@link #committer(String, String, Instant)} instead
     */
    @Deprecated
    public GHCommitBuilder committer(String name, String email, Date date) {
        return committer(name, email, GitHubClient.toInstantOrNull(date));
    }

    /**
     * Configures the committer of this commit.
     *
     * @param name
     *            the name
     * @param email
     *            the email
     * @param date
     *            the date
     * @return the gh commit builder
     */
    public GHCommitBuilder committer(String name, String email, Instant date) {
        req.with("committer", new UserInfo(name, email, date));
        return this;
    }

    /**
     * Creates a blob based on the parameters specified thus far.
     *
     * @return the gh commit
     * @throws IOException
     *             the io exception
     */
    public GHCommit create() throws IOException {
        req.with("parents", parents);
        return req.method("POST").withUrlPath(getApiTail()).fetch(GHCommit.class).wrapUp(repo);
    }

    /**
     * Message gh commit builder.
     *
     * @param message
     *            the commit message
     * @return the gh commit builder
     */
    public GHCommitBuilder message(String message) {
        req.with("message", message);
        return this;
    }

    /**
     * Parent gh commit builder.
     *
     * @param parent
     *            the SHA of a parent commit.
     * @return the gh commit builder
     */
    public GHCommitBuilder parent(String parent) {
        parents.add(parent);
        return this;
    }

    /**
     * Tree gh commit builder.
     *
     * @param tree
     *            the SHA of the tree object this commit points to
     * @return the gh commit builder
     */
    public GHCommitBuilder tree(String tree) {
        req.with("tree", tree);
        return this;
    }

    /**
     * Configures the PGP signature of this commit.
     *
     * @param signature
     *            the signature calculated from the commit
     *
     * @return the gh commit builder
     */
    public GHCommitBuilder withSignature(String signature) {
        req.with("signature", signature);
        return this;
    }

    private String getApiTail() {
        return String.format("/repos/%s/%s/git/commits", repo.getOwnerName(), repo.getName());
    }
}
