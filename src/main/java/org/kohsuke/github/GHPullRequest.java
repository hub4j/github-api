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

import static org.kohsuke.github.Previews.POLARIS;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Date;

/**
 * A pull request.
 * 
 * @author Kohsuke Kawaguchi
 * @see GHRepository#getPullRequest(int)
 */
@SuppressWarnings({"UnusedDeclaration"})
public class GHPullRequest extends GHIssue {

    private String patch_url, diff_url, issue_url;
    private GHCommitPointer base;
    private String merged_at;
    private GHCommitPointer head;

    // details that are only available when obtained from ID
    private GHUser merged_by;
    private int review_comments, additions;
    private boolean merged;
    private Boolean mergeable;
    private int deletions;
    private String mergeable_state;
    private int changed_files;
    private String merge_commit_sha;

    /**
     * GitHub doesn't return some properties of {@link GHIssue} when requesting the GET on the 'pulls' API
     * route as opposed to 'issues' API route. This flag remembers whether we made the GET call on the 'issues' route
     * on this object to fill in those missing details
     */
    private transient boolean fetchedIssueDetails;


    GHPullRequest wrapUp(GHRepository owner) {
        this.wrap(owner);
        return wrapUp(owner.root);
    }

    GHPullRequest wrapUp(GitHub root) {
        if (owner != null) owner.wrap(root);
        if (base != null) base.wrapUp(root);
        if (head != null) head.wrapUp(root);
        if (merged_by != null) merged_by.wrapUp(root);
        return this;
    }

    @Override
    protected String getApiRoute() {
        return "/repos/"+owner.getOwnerName()+"/"+owner.getName()+"/pulls/"+number;
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
    public Date getIssueUpdatedAt() throws IOException {
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
    public Collection<GHLabel> getLabels() throws IOException {
        fetchIssue();
        return super.getLabels();
    }

    @Override
    public GHUser getClosedBy() {
        return null;
    }

    @Override
    public PullRequest getPullRequest() {
        return null;
    }

    //
    // details that are only available via get with ID
    //

    public GHUser getMergedBy() throws IOException {
        populate();
        return merged_by;
    }

    public int getReviewComments() throws IOException {
        populate();
        return review_comments;
    }

    public int getAdditions() throws IOException {
        populate();
        return additions;
    }

    public boolean isMerged() throws IOException {
        populate();
        return merged;
    }

    public Boolean getMergeable() throws IOException {
        populate();
        return mergeable;
    }

    public int getDeletions() throws IOException {
        populate();
        return deletions;
    }

    public String getMergeableState() throws IOException {
        populate();
        return mergeable_state;
    }

    public int getChangedFiles() throws IOException {
        populate();
        return changed_files;
    }

    /**
     * See <a href="https://developer.github.com/changes/2013-04-25-deprecating-merge-commit-sha">GitHub blog post</a>
     */
    public String getMergeCommitSha() throws IOException {
        populate();
        return merge_commit_sha;
    }

    /**
     * Fully populate the data by retrieving missing data.
     *
     * Depending on the original API call where this object is created, it may not contain everything.
     */
    private void populate() throws IOException {
        if (merged_by!=null)    return; // already populated
        if (root.isOffline()) {
            return; // cannot populate, will have to live with what we have
        }
        root.retrieve().to(url, this).wrapUp(owner);
    }

    /**
     * Retrieves all the commits associated to this pull request.
     */
    public PagedIterable<GHPullRequestFileDetail> listFiles() {
        return new PagedIterable<GHPullRequestFileDetail>() {
            public PagedIterator<GHPullRequestFileDetail> _iterator(int pageSize) {
                return new PagedIterator<GHPullRequestFileDetail>(root.retrieve().asIterator(String.format("%s/files", getApiRoute()),
                        GHPullRequestFileDetail[].class, pageSize)) {
                    @Override
                    protected void wrapUp(GHPullRequestFileDetail[] page) {
                    }
                };
            }
        };
    }

    /**
     * Obtains all the review comments associated with this pull request.
     */
    public PagedIterable<GHPullRequestReviewComment> listReviewComments() throws IOException {
        return new PagedIterable<GHPullRequestReviewComment>() {
            public PagedIterator<GHPullRequestReviewComment> _iterator(int pageSize) {
                return new PagedIterator<GHPullRequestReviewComment>(root.retrieve().asIterator(getApiRoute() + "/comments",
                        GHPullRequestReviewComment[].class, pageSize)) {
                    protected void wrapUp(GHPullRequestReviewComment[] page) {
                        for (GHPullRequestReviewComment c : page)
                            c.wrapUp(GHPullRequest.this);
                    }
                };
            }
        };
    }

    /**
     * Retrieves all the commits associated to this pull request.
     */
    public PagedIterable<GHPullRequestCommitDetail> listCommits() {
        return new PagedIterable<GHPullRequestCommitDetail>() {
            public PagedIterator<GHPullRequestCommitDetail> _iterator(int pageSize) {
                return new PagedIterator<GHPullRequestCommitDetail>(root.retrieve().asIterator(
                        String.format("%s/commits", getApiRoute()),
                        GHPullRequestCommitDetail[].class, pageSize)) {
                    @Override
                    protected void wrapUp(GHPullRequestCommitDetail[] page) {
                        for (GHPullRequestCommitDetail c : page)
                            c.wrapUp(GHPullRequest.this);
                    }
                };
            }
        };
    }

    public GHPullRequestReviewComment createReviewComment(String body, String sha, String path, int position) throws IOException {
        return new Requester(root).method("POST")
                .with("body", body)
                .with("commit_id", sha)
                .with("path", path)
                .with("position", position)
                .to(getApiRoute() + "/comments", GHPullRequestReviewComment.class).wrapUp(this);
    }

    /**
     * Merge this pull request.
     *
     * The equivalent of the big green "Merge pull request" button.
     *
     * @param msg
     *      Commit message. If null, the default one will be used.
     */
    public void merge(String msg) throws IOException {
        merge(msg,(String)null);
    }

    /**
     * Merge this pull request.
     *
     * The equivalent of the big green "Merge pull request" button.
     *
     * @param msg
     *      Commit message. If null, the default one will be used.
     * @param sha
     *      SHA that pull request head must match to allow merge.
     */
    public void merge(String msg, String sha) throws IOException {
        new Requester(root).method("PUT").with("commit_message",msg).with("sha",sha).to(getApiRoute()+"/merge");
    }

    /**
     * Merge this pull request, using the specified merge method.
     *
     * The equivalent of the big green "Merge pull request" button.
     *
     * @param msg
     *      Commit message. If null, the default one will be used.
     * @param method
     *      SHA that pull request head must match to allow merge.
     */
    @Preview @Deprecated
    public void merge(String msg, MergeMethod method) throws IOException {
        new Requester(root).method("PUT")
		.withPreview(POLARIS)
		.with("commit_message",msg)
		.with("merge_method",method)
		.to(getApiRoute()+"/merge");
    }

    public enum MergeMethod{ MERGE, SQUASH, REBASE }

    private void fetchIssue() throws IOException {
        if (!fetchedIssueDetails) {
            new Requester(root).to(getIssuesApiRoute(), this);
            fetchedIssueDetails = true;
        }
    }
}
