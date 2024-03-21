package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.AbstractList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class GitCommit.
 *
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

    /**
     * The Class Tree.
     */
    static class Tree {

        /** The url. */
        String url;

        /** The sha. */
        String sha;

        /**
         * Gets the url.
         *
         * @return the url
         */
        public String getUrl() {
            return url;
        }

        /**
         * Gets the sha.
         *
         * @return the sha
         */
        public String getSha() {
            return sha;
        }

    }

    private Tree tree;

    private List<GHCommit.Parent> parents;

    /**
     * Instantiates a new git commit.
     */
    public GitCommit() {
        // empty constructor for Jackson binding
    };

    /**
     * Instantiates a new git commit.
     *
     * @param commit
     *            the commit
     */
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

    /**
     * Gets the tree.
     *
     * @return the tree
     */
    Tree getTree() {
        return tree;
    }

    /**
     * Gets the tree SHA 1.
     *
     * @return the tree SHA 1
     */
    public String getTreeSHA1() {
        return tree.getSha();
    }

    /**
     * Gets the tree url.
     *
     * @return the tree url
     */
    public String getTreeUrl() {
        return tree.getUrl();
    }

    /**
     * Gets the parents.
     *
     * @return the parents
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "acceptable")
    List<GHCommit.Parent> getParents() {
        return parents;
    }

    /**
     * Gets the parent SHA 1 s.
     *
     * @return the parent SHA 1 s
     */
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

    /**
     * Wrap up.
     *
     * @param owner
     *            the owner
     * @return the git commit
     */
    GitCommit wrapUp(GHRepository owner) {
        this.owner = owner;
        return this;
    }

}
