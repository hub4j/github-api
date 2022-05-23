package org.kohsuke.github;

import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.AbstractList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author Emily Xia-Reinert
 * @see GHContentUpdateResponse#getCommit() GHContentUpdateResponse#getCommit()
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
        // empty constructor for Jackson binding
    };

    GitCommit(GitCommit commit) {
        // copy constructor used to cast to GitCommit.ShortInfo and from there
        // to GHCommit, for GHContentUpdateResponse bridge method to GHCommit
        this.owner = commit.getOwner();
        this.sha = commit.getSha();
        this.node_id = commit.getNodeId();
        this.url = commit.getUrl();
        this.html_url = commit.getHtmlUrl();
        this.author = commit.getAuthor();
        this.committer = commit.getCommitter();
        this.message = commit.getMessage();
        this.verification = commit.getVerification();
        this.tree = commit.getTree();
        this.parents = commit.getParents();
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
    @WithBridgeMethods(value = GHCommit.GHAuthor.class, adapterMethod = "gitUserToGHAuthor")
    public GitUser getAuthor() {
        return author;
    }

    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "bridge method of getAuthor & getCommitter")
    private Object gitUserToGHAuthor(GitUser author, Class targetType) {
        return new GHCommit.GHAuthor(author);
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
    @WithBridgeMethods(value = GHCommit.GHAuthor.class, adapterMethod = "gitUserToGHAuthor")
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

    Tree getTree() {
        return tree;
    }

    public String getTreeSHA1() {
        return tree.getSha();
    }

    public String getTreeUrl() {
        return tree.getUrl();
    }

    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "acceptable")
    List<GHCommit.Parent> getParents() {
        return parents;
    }

    public List<String> getParentSHA1s() {
        if (parents == null || parents.size() == 0)
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

    GitCommit wrapUp(GHRepository owner) {
        this.owner = owner;
        return this;
    }

}
