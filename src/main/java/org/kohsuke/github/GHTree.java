/*
 * GitHub API for Java
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
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
 * @see GHCommit#getTree()
 * @see GHRepository#getTree(String)
 * @see GHTreeEntry#asTree()
 */
public class GHTree {
    /* package almost final */GHRepository repo;

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

    /* package */GHTree wrap(GHRepository repo) {
        this.repo = repo;
        for (GHTreeEntry e : tree) {
            e.tree = this;
        }
        return this;
    }

}
