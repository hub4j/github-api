package org.kohsuke.github;

import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Date;

/**
 * A commit in a repository.
 *
 * @author Emily Xia-Reinert TODO: update @see (what are these)
 * @see GHCommitComment#getCommit() GHCommitComment#getCommit()
 */
@SuppressFBWarnings(value = { "NP_UNWRITTEN_FIELD", "UWF_UNWRITTEN_FIELD" }, justification = "JSON API")
public class GitCommit {
    private GHRepository owner;
    private String sha, url;
    private GitUser author;
    private GitUser committer;

    private String message;

    private GHVerification verification;

    static class Tree {
        String url;
        String sha;

        public String getUrl() {
            return url;
        }

        public String getSha() {
            return sha;
        }

    }

    private Tree tree;

    public GitCommit(){

    };

    public GitCommit(GitCommit commit) {
        this.owner = commit.getOwner();
        this.sha = commit.getSha();
        this.url = commit.getUrl();
        this.author = commit.getAuthor();
        this.committer = commit.getCommitter();
        this.message = commit.getMessage();
        this.verification = commit.getVerification();
        this.tree = commit.getTree();
    }

    /**
     * Gets owner.
     *
     * @return the repository that contains the commit.
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHRepository getOwner() {
        return owner;
    }

    /**
     * Gets SHA1.
     *
     * @return The SHA1 of this commit
     */
    public String getSHA1() {
        return sha;
    }

    /**
     * Gets SHA.
     *
     * @return The SHA of this commit
     */
    public String getSha() {
        return sha;
    }

    /**
     * Gets URL.
     *
     * @return The URL of this commit
     */
    public String getUrl() {
        return url;
    }

    /**
     * Gets author.
     *
     * @return the author
     */
    @WithBridgeMethods(value = GHAuthor.class, castRequired = true)
    public GitUser getAuthor() {
        return author;
    }

    /**
     * Gets authored date.
     *
     * @return the authored date
     */
    public Date getAuthoredDate() {
        return author.getDate();
    }

    /**
     * Gets committer.
     *
     * @return the committer
     */
    @WithBridgeMethods(value = GHAuthor.class, castRequired = true)
    public GitUser getCommitter() {
        return committer;
    }

    /**
     * Gets commit date.
     *
     * @return the commit date
     */
    public Date getCommitDate() {
        return committer.getDate();
    }

    /**
     * Gets message.
     *
     * @return Commit message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Gets Verification Status.
     *
     * @return the Verification status
     */
    public GHVerification getVerification() {
        return verification;
    }

    public Tree getTree() {
        return tree;
    }

    /**
     * The type GHAuthor.
     *
     * @deprecated Use {@link GitUser} instead.
     */
    public static class GHAuthor extends GitUser {
    }

    GitCommit wrapUp(GHRepository owner) {
        this.owner = owner;
        return this;
    }

}
