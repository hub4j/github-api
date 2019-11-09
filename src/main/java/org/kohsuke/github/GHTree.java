package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Provides information for Git Trees
 * https://developer.github.com/v3/git/trees/
 *
 * @author Daniel Teixeira - https://github.com/ddtxra
 * @see GHCommit#getTree()
 * @see GHRepository#getTree(String)
 * @see GHTreeEntry#asTree()
 */
public class GHTree {

    @JacksonInject(value = "org.kohsuke.github.GHRepository")
    GHRepository repo;

    private boolean truncated;
    private String sha, url;
    private GHTreeEntry[] tree;

    /**
     * The SHA for this trees
     */
    public String getSha() {
        return sha;
    }

    /**
     * Return an array of entries of the trees
     */
    public List<GHTreeEntry> getTree() {
        return Collections.unmodifiableList(Arrays.asList(tree));
    }

    @JsonSetter("tree")
    void setTree(GHTreeEntry[] tree) {
        this.tree = tree;
        for (GHTreeEntry e : tree) {
            e.tree = this;
        }
    }


    /**
     * Finds a tree entry by its name.
     *
     * IOW, find a directory entry by a file name.
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
     * @return true true if the number of items in the tree array exceeded the GitHub maximum limit otherwise false.
     */
    public boolean isTruncated() {
        return truncated;
    }

    /**
     * The API URL of this tag, such as
     * "url": "https://api.github.com/repos/octocat/Hello-World/trees/fc6274d15fa3ae2ab983129fb037999f264ba9a7",
     */
    public URL getUrl() {
        return GitHub.parseURL(url);
    }

}
