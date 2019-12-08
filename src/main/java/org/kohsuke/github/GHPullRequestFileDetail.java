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

/**
 * File detail inside a {@link GHPullRequest}.
 *
 * @author Julien Henry
 * @see GHPullRequest#listFiles() GHPullRequest#listFiles()
 */
public class GHPullRequestFileDetail {

    String sha;
    String filename;
    String status;
    int additions;
    int deletions;
    int changes;
    String blob_url;
    String raw_url;
    String contents_url;
    String patch;
    String previous_filename;

    /**
     * Gets sha.
     *
     * @return the sha
     */
    public String getSha() {
        return sha;
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
     * Gets status.
     *
     * @return the status
     */
    public String getStatus() {
        return status;
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
     * Gets deletions.
     *
     * @return the deletions
     */
    public int getDeletions() {
        return deletions;
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
     * Gets blob url.
     *
     * @return the blob url
     */
    public URL getBlobUrl() {
        return GitHub.parseURL(blob_url);
    }

    /**
     * Gets raw url.
     *
     * @return the raw url
     */
    public URL getRawUrl() {
        return GitHub.parseURL(raw_url);
    }

    /**
     * Gets contents url.
     *
     * @return the contents url
     */
    public URL getContentsUrl() {
        return GitHub.parseURL(contents_url);
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
        return previous_filename;
    }
}
