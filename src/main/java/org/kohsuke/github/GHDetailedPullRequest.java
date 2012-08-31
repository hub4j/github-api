/*
 * The MIT License
 *
 * Copyright 2012 Honza Brázdil.
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
 *
 * @author Honza Brázdil
 */
public class GHDetailedPullRequest extends GHPullRequest {
	private GHSmallUser merged_by;
	private int review_comments, additions;
	private boolean merged;
	private Boolean mergeable;
	private int deletions;
	private String mergeable_state;
	private int changed_files;
	
	@Override
	GHDetailedPullRequest wrapUp(GHRepository owner){
		super.wrapUp(owner);
		if(merged_by != null) merged_by.wrapUp(root);
		return this;
	}

	public GHSmallUser getMerged_by() {
		return merged_by;
	}

	public int getReview_comments() {
		return review_comments;
	}

	public int getAdditions() {
		return additions;
	}

	public boolean isMerged() {
		return merged;
	}

	public Boolean getMergeable() {
		return mergeable;
	}

	public int getDeletions() {
		return deletions;
	}

	public String getMergeable_state() {
		return mergeable_state;
	}

	public int getChanged_files() {
		return changed_files;
	}
}
