package org.kohsuke.github;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * Provides information for Git Trees https://developer.github.com/v3/git/trees/
 *
 * @author Daniel Teixeira - https://github.com/ddtxra
 * @see GHCommit#getTree() GHCommit#getTree()
 * @see GHRepository#getTree(String) GHRepository#getTree(String)
 * @see GHTreeEntry#asTree() GHTreeEntry#asTree()
 */
public class GHTree {

    /** The repo. */
    /* package almost final */GHRepository repo;

    private boolean truncated;
    private String sha, url;
    private GHTreeEntry[] tree;

    /**
     * The SHA for this trees.
     *
     * @return the sha
     */
    public String getSha() {
        return sha;
    }

    /**
     * Return an array of entries of the trees.
     *
     * @return the tree
     */
    public List<GHTreeEntry> getTree() {
        return Collections.unmodifiableList(Arrays.asList(tree));
    }

    /**
     * Finds a tree entry by its name.
     * <p>
     * IOW, find a directory entry by a file name.
     *
     * @param path
     *            the path
     * @return the entry
     */
    public GHTreeEntry getEntry(String path) {
        for (GHTreeEntry e : tree) {
            if (e.getPath().equals(path))
                return e;
        }
        return null;
    }

    /**
     * Returns true if the number of items in the tree array exceeded the GitHub maximum limit.
     *
     * @return true true if the number of items in the tree array exceeded the GitHub maximum limit otherwise false.
     */
    public boolean isTruncated() {
        return truncated;
    }

    /**
     * The API URL of this tag, such as "url":
     * "https://api.github.com/repos/octocat/Hello-World/trees/fc6274d15fa3ae2ab983129fb037999f264ba9a7",
     *
     * @return the url
     */
    public URL getUrl() {
        return GitHubClient.parseURL(url);
    }

    /**
     * Wrap.
     *
     * @param repo
     *            the repo
     * @return the GH tree
     */
    GHTree wrap(GHRepository repo) {
        this.repo = repo;
        for (GHTreeEntry e : tree) {
            e.tree = this;
        }
        return this;
    }

}
