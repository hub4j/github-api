/*
 * The MIT License
 *
 * Copyright (c) 2010, Kohsuke Kawaguchi
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
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.kohsuke.github;

/**
 * Identifies a commit in {@link GHPullRequest}.
 *
 * @author Kohsuke Kawaguchi
 */
public class GHCommitPointer {
    private String ref, sha, label;
    private GHUser user;
    private GHRepository repository/*V2*/,repo/*V3*/;

    /**
     * This points to the user who owns
     * the {@link #repository}.
     */
    public GHUser getUser() {
        return user;
    }

    /**
     * The repository that contains the commit.
     */
    public GHRepository getRepository() {
        return repo!=null ? repo : repository;
    }

    /**
     * Named ref to the commit. This appears to be a "short ref" that doesn't include "refs/heads/" portion.
     */
    public String getRef() {
        return ref;
    }

    /**
     * SHA1 of the commit.
     */
    public String getSha() {
        return sha;
    }

    /**
     * String that looks like "USERNAME:REF".
     */
    public String getLabel() {
        return label;
    }
}
