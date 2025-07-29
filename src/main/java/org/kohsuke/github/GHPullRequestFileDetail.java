/*
 * The MIT License
 *
 * Copyright (c) 2015, Julien Henry
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.kohsuke.github;

import java.net.URL;

// TODO: Auto-generated Javadoc
/**
 * File detail inside a {@link GHPullRequest}.
 *
 * @author Julien Henry
 * @see GHPullRequest#listFiles() GHPullRequest#listFiles()
 * @see <a href="https://docs.github.com/en/rest/reference/pulls#list-pull-requests-files">List pull requests files</a>
 */
public class GHPullRequestFileDetail {

    /** The additions. */
    int additions;

    /** The blob url. */
    String blobUrl;

    /** The changes. */
    int changes;

    /** The contents url. */
    String contentsUrl;

    /** The deletions. */
    int deletions;

    /** The filename. */
    String filename;

    /** The patch. */
    String patch;

    /** The previous filename. */
    String previousFilename;

    /** The raw url. */
    String rawUrl;

    /** The sha. */
    String sha;

    /** The status. */
    String status;

    /**
     * Create default GHPullRequestFileDetail instance
     */
    public GHPullRequestFileDetail() {
    }

    /**
     * Gets additions.
     *
     * @return the additions
     */
    public int getAdditions() {
        return additions;
    }

    /**
     * Gets blob url.
     *
     * @return the blob url
     */
    public URL getBlobUrl() {
        return GitHubClient.parseURL(blobUrl);
    }

    /**
     * Gets changes.
     *
     * @return the changes
     */
    public int getChanges() {
        return changes;
    }

    /**
     * Gets contents url.
     *
     * @return the contents url
     */
    public URL getContentsUrl() {
        return GitHubClient.parseURL(contentsUrl);
    }

    /**
     * Gets deletions.
     *
     * @return the deletions
     */
    public int getDeletions() {
        return deletions;
    }

    /**
     * Gets filename.
     *
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Gets patch.
     *
     * @return the patch
     */
    public String getPatch() {
        return patch;
    }

    /**
     * Gets previous filename.
     *
     * @return the previous filename
     */
    public String getPreviousFilename() {
        return previousFilename;
    }

    /**
     * Gets raw url.
     *
     * @return the raw url
     */
    public URL getRawUrl() {
        return GitHubClient.parseURL(rawUrl);
    }

    /**
     * Gets sha of the file (not commit sha).
     *
     * @return the sha
     * @see <a href="https://docs.github.com/en/rest/reference/pulls#list-pull-requests-files">List pull requests
     *      files</a>
     */
    public String getSha() {
        return sha;
    }

    /**
     * Gets status (added/modified/deleted).
     *
     * @return the status
     */
    public String getStatus() {
        return status;
    }
}
