package org.kohsuke.github;

import java.net.URL;

/**
 * Provides information for Git Trees
 * https://developer.github.com/v3/git/trees/
 *
 * @author Daniel Teixeira - https://github.com/ddtxra
 * @see GHTree
 */
public class GHTreeEntry {
    /* package almost final */GHTree tree;

    private String path, mode, type, sha, url;
    private long size;

    /**
     * Get the path such as
     * "subdir/file.txt"
     *
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * Get mode such as
     * 100644
     *
     * @return the mode
     */
    public String getMode() {
        return mode;
    }

    /**
     * Gets the size of the file, such as
     * 132
     * @return The size of the path or 0 if it is a directory
     */
    public long getSize() {
        return size;
    }

    /**
     * Gets the type such as:
     * "blob", "tree", etc.
     *
     * @return The type
     */
    public String getType() {
        return type;
    }


    /**
     * SHA1 of this object.
     */
    public String getSha() {
        return sha;
    }

    /**
     * API URL to this Git data, such as
     * https://api.github.com/repos/jenkinsci
     * /jenkins/git/commits/b72322675eb0114363a9a86e9ad5a170d1d07ac0
     */
    public URL getUrl() {
        return GitHub.parseURL(url);
    }
}
