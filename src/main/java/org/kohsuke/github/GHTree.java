package org.kohsuke.github;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Provides information for Git Trees
 * https://developer.github.com/v3/git/trees/
 *
 * @author Daniel Teixeira - https://github.com/ddtxra
 * @see GHRepository#getTree(String)
 */
public class GHTree {
    /* package almost final */GitHub root;

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
     * @return
     */
    public List<GHTreeEntry> getTree() {
        return Collections.unmodifiableList(Arrays.asList(tree));
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

    /* package */GHTree wrap(GitHub root) {
        this.root = root;
        return this;
    }

}
