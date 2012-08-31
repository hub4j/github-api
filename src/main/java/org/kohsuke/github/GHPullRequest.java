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

import java.net.URL;
import java.util.Collection;
import java.util.Date;

/**
 * A pull request.
 * 
 * @author Kohsuke Kawaguchi
 */
@SuppressWarnings({"UnusedDeclaration"})
public class GHPullRequest extends GHIssue {
	
	private String patch_url, diff_url, issue_url;
	private GHCommitPointer base;
	private String merged_at;
	private GHCommitPointer head;

	GHPullRequest wrapUp(GHRepository owner) {
		this.wrap(owner);
        return wrapUp(owner.root);
    }
	
    GHPullRequest wrapUp(GitHub root) {
        if (owner!=null)    owner.wrap(root);
        if (base!=null)     base.wrapUp(root);
        if (head!=null)     head.wrapUp(root);
        return this;
    }
	
    /**
     * The URL of the patch file.
     * like https://github.com/jenkinsci/jenkins/pull/100.patch
     */
    public URL getPatchUrl() {
        return GitHub.parseURL(patch_url);
    }
	
	/**
     * The URL of the patch file.
     * like https://github.com/jenkinsci/jenkins/pull/100.patch
     */
    public URL getIssueUrl() {
        return GitHub.parseURL(issue_url);
    }

    /**
     * This points to where the change should be pulled into,
     * but I'm not really sure what exactly it means.
     */
    public GHCommitPointer getBase() {
        return base;
    }

    /**
     * The change that should be pulled. The tip of the commits to merge.
     */
    public GHCommitPointer getHead() {
        return head;
    }

	@Deprecated
    public Date getIssueUpdatedAt() {
        return super.getUpdatedAt();
    }

    /**
     * The diff file,
     * like https://github.com/jenkinsci/jenkins/pull/100.diff
     */
    public URL getDiffUrl() {
        return GitHub.parseURL(diff_url);
    }

    public Date getMergedAt() {
        return GitHub.parseDate(merged_at);
    }

	@Override
	public Collection<String> getLabels() {
		return super.getLabels();
	}

	@Override
	public GHSmallUser getClosedBy() {
		return null;
	}

	@Override
	public PullRequest getPullRequest() {
		return null;
	}
}
