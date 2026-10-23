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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;

// TODO: Auto-generated Javadoc
/**
 * Identifies a commit in {@link GHPullRequest}.
 *
 * @author Kohsuke Kawaguchi
 */
public class GHCommitPointer {

    /** The pull request this pointer belongs to, used when {@link #repo} is unavailable. */
    private transient GHPullRequest pullRequest;

    private String ref, sha, label;
    private GHRepository repo;

    private GHUser user;
    /**
     * Create default GHCommitPointer instance
     */
    public GHCommitPointer() {
    }

    /**
     * Obtains the commit that this pointer is referring to.
     *
     * If the repository is unavailable (for example, the head repository of a pull request opened from a fork that has
     * since been deleted), the commit is looked up in the repository of the pull request instead. GitHub keeps pull
     * request commits in the base repository, so they remain reachable there.
     *
     * @return the commit
     * @throws IOException
     *             the io exception
     */
    public GHCommit getCommit() throws IOException {
        GHRepository repository = getRepository();
        if (repository == null && pullRequest != null) {
            repository = pullRequest.getRepository();
        }
        if (repository == null) {
            throw new IOException("Repository information is unavailable for commit " + getSha());
        }
        return repository.getCommit(getSha());
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
     * Named ref to the commit. This appears to be a "short ref" that doesn't include "refs/heads/" portion.
     *
     * @return the ref
     */
    public String getRef() {
        return ref;
    }

    /**
     * The repository that contains the commit.
     *
     * This value is {@code null} when GitHub does not report a repository, for example when the head repository of a
     * pull request has been deleted. {@link #getCommit()} still works in that case.
     *
     * @return the repository, or {@code null} when it is unavailable
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHRepository getRepository() {
        return repo;
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
     * This points to the user who owns the {@link #getRepository()}.
     *
     * @return the user
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHUser getUser() {
        if (user != null)
            return user.root().intern(user);
        return user;
    }

    /**
     * Binds this pointer to the pull request it belongs to.
     *
     * @param pullRequest
     *            the pull request
     * @return this pointer
     */
    GHCommitPointer wrapUp(GHPullRequest pullRequest) {
        this.pullRequest = pullRequest;
        return this;
    }
}
