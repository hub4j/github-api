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

import java.io.IOException;

/**
 * Identifies a commit in {@link GHPullRequest}.
 */
public class GHCommitPointer {
    private String ref, sha, label;
    private GHUser user;
    private GHRepository repo;

    /**
     * This points to the user who owns the {@link #getRepository()}.
     *
     * @return the user
     * @throws IOException
     *             the io exception
     */
    public GHUser getUser() throws IOException {
        if (user != null)
            return user.root.intern(user);
        return user;
    }

    /**
     * The repository that contains the commit.
     *
     * @return the repository
     */
    public GHRepository getRepository() {
        return repo;
    }

    /**
     * Named ref to the commit. This appears to be a "short ref" that doesn't include "refs/heads/" portion.
     *
     * @return the ref
     */
    public String getRef() {
        return ref;
    }

    /**
     * SHA1 of the commit.
     *
     * @return the sha
     */
    public String getSha() {
        return sha;
    }

    /**
     * String that looks like "USERNAME:REF".
     *
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Obtains the commit that this pointer is referring to.
     *
     * @return the commit
     * @throws IOException
     *             the io exception
     */
    public GHCommit getCommit() throws IOException {
        return getRepository().getCommit(getSha());
    }

    void wrapUp(GitHub root) {
        if (user != null)
            user.root = root;
        if (repo != null)
            repo.wrap(root);
    }
}
