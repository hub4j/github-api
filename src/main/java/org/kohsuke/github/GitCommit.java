package org.kohsuke.github;

import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * A commit in a repository.
 *
 * @author Emily Xia-Reinert TODO: update @see (what are these)
 * @see GHCommitComment#getCommit() GHCommitComment#getCommit()
 */
@SuppressFBWarnings(value = { "NP_UNWRITTEN_FIELD", "UWF_UNWRITTEN_FIELD" }, justification = "JSON API")
public class GitCommit {
    private GHRepository owner;
    private String sha, node_id, url, html_url;
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

    private List<GHCommit.Parent> parents;

    public GitCommit() {

    };

    public GitCommit(GitCommit commit) {
        this.owner = commit.getOwner();
        this.sha = commit.getSha();
        this.node_id = commit.getNodeId();
        this.html_url = commit.getHtmlUrl();
        this.url = commit.getUrl();
        this.author = commit.getAuthor();
        this.committer = commit.getCommitter();
        this.message = commit.getMessage();
        this.verification = commit.getVerification();
        this.tree = commit.getTree();
        this.parents = commit.getRawParents();
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
     * Gets node id.
     *
     * @return The node id of this commit
     */
    public String getNodeId() {
        return node_id;
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
     * Gets HTML URL.
     *
     * @return The HTML URL of this commit
     */
    public String getHtmlUrl() {
        return html_url;
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

    public List<GHCommit> getParents() throws IOException {
        List<GHCommit> r = new ArrayList<GHCommit>();
        for (String sha1 : getParentSHA1s())
            r.add(owner.getCommit(sha1));
        return r;
    }

    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "acceptable")
    public List<GHCommit.Parent> getRawParents() {
        return parents;
    }

    public List<String> getParentSHA1s() {
        if (parents == null)
            return Collections.emptyList();
        return new AbstractList<String>() {
            @Override
            public String get(int index) {
                return parents.get(index).sha;
            }

            @Override
            public int size() {
                return parents.size();
            }
        };
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
