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
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.annotation.CheckForNull;

import static org.kohsuke.github.internal.Previews.LYDIAN;
import static org.kohsuke.github.internal.Previews.SHADOW_CAT;

// TODO: Auto-generated Javadoc
/**
 * A pull request.
 *
 * @author Kohsuke Kawaguchi
 * @see GHRepository#getPullRequest(int) GHRepository#getPullRequest(int)
 */
@SuppressWarnings({ "UnusedDeclaration" })
public class GHPullRequest extends GHIssue implements Refreshable {

    private static final String COMMENTS_ACTION = "/comments";
    private static final String REQUEST_REVIEWERS = "/requested_reviewers";

    private String patch_url, diff_url, issue_url;
    private GHCommitPointer base;
    private String merged_at;
    private GHCommitPointer head;

    // details that are only available when obtained from ID
    private GHUser merged_by;
    private int review_comments, additions, commits;
    private boolean merged, maintainer_can_modify;

    /** The draft. */
    // making these package private to all for testing
    boolean draft;
    private Boolean mergeable;
    private int deletions;
    private String mergeable_state;
    private int changed_files;
    private String merge_commit_sha;
    private AutoMerge auto_merge;

    // pull request reviewers

    private GHUser[] requested_reviewers;
    private GHTeam[] requested_teams;

    /**
     * Wrap up.
     *
     * @param owner
     *            the owner
     * @return the GH pull request
     */
    GHPullRequest wrapUp(GHRepository owner) {
        this.wrap(owner);
        return this;
    }

    /**
     * Gets the api route.
     *
     * @return the api route
     */
    @Override
    protected String getApiRoute() {
        if (owner == null) {
            // Issues returned from search to do not have an owner. Attempt to use url.
            final URL url = Objects.requireNonNull(getUrl(), "Missing instance URL!");
            return StringUtils.prependIfMissing(url.toString().replace(root().getApiUrl(), ""), "/");
        }
        return "/repos/" + owner.getOwnerName() + "/" + owner.getName() + "/pulls/" + number;
    }

    /**
     * The status of auto merging a pull request.
     *
     * @return the {@linkplain AutoMerge} or {@code null} if no auto merge is set.
     */
    public AutoMerge getAutoMerge() {
        return auto_merge;
    }

    /**
     * The URL of the patch file. like https://github.com/jenkinsci/jenkins/pull/100.patch
     *
     * @return the patch url
     */
    public URL getPatchUrl() {
        return GitHubClient.parseURL(patch_url);
    }

    /**
     * The URL of the patch file. like https://github.com/jenkinsci/jenkins/pull/100.patch
     *
     * @return the issue url
     */
    public URL getIssueUrl() {
        return GitHubClient.parseURL(issue_url);
    }

    /**
     * This points to where the change should be pulled into, but I'm not really sure what exactly it means.
     *
     * @return the base
     */
    public GHCommitPointer getBase() {
        return base;
    }

    /**
     * The change that should be pulled. The tip of the commits to merge.
     *
     * @return the head
     */
    public GHCommitPointer getHead() {
        return head;
    }

    /**
     * Gets issue updated at.
     *
     * @return the issue updated at
     * @throws IOException
     *             the io exception
     */
    @Deprecated
    public Date getIssueUpdatedAt() throws IOException {
        return super.getUpdatedAt();
    }

    /**
     * The diff file, like https://github.com/jenkinsci/jenkins/pull/100.diff
     *
     * @return the diff url
     */
    public URL getDiffUrl() {
        return GitHubClient.parseURL(diff_url);
    }

    /**
     * Gets merged at.
     *
     * @return the merged at
     */
    public Date getMergedAt() {
        return GitHubClient.parseDate(merged_at);
    }

    /**
     * Gets the closed by.
     *
     * @return the closed by
     */
    @Override
    public GHUser getClosedBy() {
        return null;
    }

    /**
     * Gets the pull request.
     *
     * @return the pull request
     */
    @Override
    public PullRequest getPullRequest() {
        return null;
    }

    //
    // details that are only available via get with ID
    //

    /**
     * Gets merged by.
     *
     * @return the merged by
     * @throws IOException
     *             the io exception
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHUser getMergedBy() throws IOException {
        populate();
        return merged_by;
    }

    /**
     * Gets review comments.
     *
     * @return the review comments
     * @throws IOException
     *             the io exception
     */
    public int getReviewComments() throws IOException {
        populate();
        return review_comments;
    }

    /**
     * Gets additions.
     *
     * @return the additions
     * @throws IOException
     *             the io exception
     */
    public int getAdditions() throws IOException {
        populate();
        return additions;
    }

    /**
     * Gets the number of commits.
     *
     * @return the number of commits
     * @throws IOException
     *             the io exception
     */
    public int getCommits() throws IOException {
        populate();
        return commits;
    }

    /**
     * Is merged boolean.
     *
     * @return the boolean
     * @throws IOException
     *             the io exception
     */
    public boolean isMerged() throws IOException {
        populate();
        return merged;
    }

    /**
     * Can maintainer modify boolean.
     *
     * @return the boolean
     * @throws IOException
     *             the io exception
     */
    public boolean canMaintainerModify() throws IOException {
        populate();
        return maintainer_can_modify;
    }

    /**
     * Is draft boolean.
     *
     * @return the boolean
     * @throws IOException
     *             the io exception
     */
    public boolean isDraft() throws IOException {
        populate();
        return draft;
    }

    /**
     * Is this PR mergeable?.
     *
     * @return null if the state has not been determined yet, for example when a PR is newly created. If this method is
     *         called on an instance whose mergeable state is not yet known, API call is made to retrieve the latest
     *         state.
     * @throws IOException
     *             the io exception
     */
    public Boolean getMergeable() throws IOException {
        refresh(mergeable);
        return mergeable;
    }

    /**
     * for test purposes only.
     *
     * @return the mergeable no refresh
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Deprecated
    Boolean getMergeableNoRefresh() throws IOException {
        return mergeable;
    }

    /**
     * Gets deletions.
     *
     * @return the deletions
     * @throws IOException
     *             the io exception
     */
    public int getDeletions() throws IOException {
        populate();
        return deletions;
    }

    /**
     * Gets mergeable state.
     *
     * @return the mergeable state
     * @throws IOException
     *             the io exception
     */
    public String getMergeableState() throws IOException {
        populate();
        return mergeable_state;
    }

    /**
     * Gets changed files.
     *
     * @return the changed files
     * @throws IOException
     *             the io exception
     */
    public int getChangedFiles() throws IOException {
        populate();
        return changed_files;
    }

    /**
     * See <a href="https://developer.github.com/changes/2013-04-25-deprecating-merge-commit-sha">GitHub blog post</a>
     *
     * @return the merge commit sha
     * @throws IOException
     *             the io exception
     */
    public String getMergeCommitSha() throws IOException {
        populate();
        return merge_commit_sha;
    }

    /**
     * Gets requested reviewers.
     *
     * @return the requested reviewers
     * @throws IOException
     *             the io exception
     */
    public List<GHUser> getRequestedReviewers() throws IOException {
        refresh(requested_reviewers);
        return Collections.unmodifiableList(Arrays.asList(requested_reviewers));
    }

    /**
     * Gets requested teams.
     *
     * @return the requested teams
     * @throws IOException
     *             the io exception
     */
    public List<GHTeam> getRequestedTeams() throws IOException {
        refresh(requested_teams);
        return Collections.unmodifiableList(Arrays.asList(requested_teams));
    }

    /**
     * Fully populate the data by retrieving missing data.
     *
     * <p>
     * Depending on the original API call where this object is created, it may not contain everything.
     */
    private void populate() throws IOException {
        if (mergeable_state != null)
            return; // already populated
        refresh();
    }

    /**
     * Repopulates this object.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void refresh() throws IOException {
        if (isOffline()) {
            return; // cannot populate, will have to live with what we have
        }

        URL url = getUrl();
        if (url != null) {
            root().createRequest().withPreview(SHADOW_CAT).setRawUrlPath(url.toString()).fetchInto(this).wrapUp(owner);
        }
    }

    /**
     * Retrieves all the files associated to this pull request. The paginated response returns 30 files per page by
     * default.
     *
     * @return the paged iterable
     * @see <a href="https://docs.github.com/en/rest/reference/pulls#list-pull-requests-files">List pull requests
     *      files</a>
     */
    public PagedIterable<GHPullRequestFileDetail> listFiles() {
        return root().createRequest()
                .withUrlPath(String.format("%s/files", getApiRoute()))
                .toIterable(GHPullRequestFileDetail[].class, null);
    }

    /**
     * Retrieves all the reviews associated to this pull request.
     *
     * @return the paged iterable
     */
    public PagedIterable<GHPullRequestReview> listReviews() {
        return root().createRequest()
                .withUrlPath(String.format("%s/reviews", getApiRoute()))
                .toIterable(GHPullRequestReview[].class, item -> item.wrapUp(this));
    }

    /**
     * Obtains all the review comments associated with this pull request.
     *
     * @return the paged iterable
     * @throws IOException
     *             the io exception
     */
    public PagedIterable<GHPullRequestReviewComment> listReviewComments() throws IOException {
        return root().createRequest()
                .withUrlPath(getApiRoute() + COMMENTS_ACTION)
                .toIterable(GHPullRequestReviewComment[].class, item -> item.wrapUp(this));
    }

    /**
     * Retrieves all the commits associated to this pull request.
     *
     * @return the paged iterable
     */
    public PagedIterable<GHPullRequestCommitDetail> listCommits() {
        return root().createRequest()
                .withUrlPath(String.format("%s/commits", getApiRoute()))
                .toIterable(GHPullRequestCommitDetail[].class, item -> item.wrapUp(this));
    }

    /**
     * Create review gh pull request review.
     *
     * @param body
     *            the body
     * @param event
     *            the event
     * @param comments
     *            the comments
     * @return the gh pull request review
     * @throws IOException
     *             the io exception
     * @deprecated Use {@link #createReview()}
     */
    @Deprecated
    public GHPullRequestReview createReview(String body,
            @CheckForNull GHPullRequestReviewState event,
            GHPullRequestReviewComment... comments) throws IOException {
        return createReview(body, event, Arrays.asList(comments));
    }

    /**
     * Create review gh pull request review.
     *
     * @param body
     *            the body
     * @param event
     *            the event
     * @param comments
     *            the comments
     * @return the gh pull request review
     * @throws IOException
     *             the io exception
     * @deprecated Use {@link #createReview()}
     */
    @Deprecated
    public GHPullRequestReview createReview(String body,
            @CheckForNull GHPullRequestReviewState event,
            List<GHPullRequestReviewComment> comments) throws IOException {
        GHPullRequestReviewBuilder b = createReview().body(body);
        for (GHPullRequestReviewComment c : comments) {
            b.comment(c.getBody(), c.getPath(), c.getPosition());
        }
        return b.create();
    }

    /**
     * Create review gh pull request review builder.
     *
     * @return the gh pull request review builder
     */
    public GHPullRequestReviewBuilder createReview() {
        return new GHPullRequestReviewBuilder(this);
    }

    /**
     * Create review comment gh pull request review comment.
     *
     * @param body
     *            the body
     * @param sha
     *            the sha
     * @param path
     *            the path
     * @param position
     *            the position
     * @return the gh pull request review comment
     * @throws IOException
     *             the io exception
     */
    public GHPullRequestReviewComment createReviewComment(String body, String sha, String path, int position)
            throws IOException {
        return root().createRequest()
                .method("POST")
                .with("body", body)
                .with("commit_id", sha)
                .with("path", path)
                .with("position", position)
                .withUrlPath(getApiRoute() + COMMENTS_ACTION)
                .fetch(GHPullRequestReviewComment.class)
                .wrapUp(this);
    }

    /**
     * Request reviewers.
     *
     * @param reviewers
     *            the reviewers
     * @throws IOException
     *             the io exception
     */
    public void requestReviewers(List<GHUser> reviewers) throws IOException {
        root().createRequest()
                .method("POST")
                .with("reviewers", getLogins(reviewers))
                .withUrlPath(getApiRoute() + REQUEST_REVIEWERS)
                .send();
    }

    /**
     * Request team reviewers.
     *
     * @param teams
     *            the teams
     * @throws IOException
     *             the io exception
     */
    public void requestTeamReviewers(List<GHTeam> teams) throws IOException {
        List<String> teamReviewers = new ArrayList<String>(teams.size());
        for (GHTeam team : teams) {
            teamReviewers.add(team.getSlug());
        }
        root().createRequest()
                .method("POST")
                .with("team_reviewers", teamReviewers)
                .withUrlPath(getApiRoute() + REQUEST_REVIEWERS)
                .send();
    }

    /**
     * Set the base branch on the pull request.
     *
     * @param newBaseBranch
     *            the name of the new base branch
     * @return the updated pull request
     * @throws IOException
     *             the io exception
     */
    public GHPullRequest setBaseBranch(String newBaseBranch) throws IOException {
        return root().createRequest()
                .method("PATCH")
                .with("base", newBaseBranch)
                .withUrlPath(getApiRoute())
                .fetch(GHPullRequest.class);
    }

    /**
     * Updates the branch. The same as pressing the button in the web GUI.
     *
     * @throws IOException
     *             the io exception
     */
    @Preview(LYDIAN)
    public void updateBranch() throws IOException {
        root().createRequest()
                .withPreview(LYDIAN)
                .method("PUT")
                .with("expected_head_sha", head.getSha())
                .withUrlPath(getApiRoute() + "/update-branch")
                .send();
    }

    /**
     * Merge this pull request.
     *
     * <p>
     * The equivalent of the big green "Merge pull request" button.
     *
     * @param msg
     *            Commit message. If null, the default one will be used.
     * @throws IOException
     *             the io exception
     */
    public void merge(String msg) throws IOException {
        merge(msg, null);
    }

    /**
     * Merge this pull request.
     *
     * <p>
     * The equivalent of the big green "Merge pull request" button.
     *
     * @param msg
     *            Commit message. If null, the default one will be used.
     * @param sha
     *            SHA that pull request head must match to allow merge.
     * @throws IOException
     *             the io exception
     */
    public void merge(String msg, String sha) throws IOException {
        merge(msg, sha, null);
    }

    /**
     * Merge this pull request, using the specified merge method.
     *
     * <p>
     * The equivalent of the big green "Merge pull request" button.
     *
     * @param msg
     *            Commit message. If null, the default one will be used.
     * @param sha
     *            the sha
     * @param method
     *            SHA that pull request head must match to allow merge.
     * @throws IOException
     *             the io exception
     */
    public void merge(String msg, String sha, MergeMethod method) throws IOException {
        root().createRequest()
                .method("PUT")
                .with("commit_message", msg)
                .with("sha", sha)
                .with("merge_method", method)
                .withUrlPath(getApiRoute() + "/merge")
                .send();
    }

    /** The enum MergeMethod. */
    public enum MergeMethod {

        /** The merge. */
        MERGE,
        /** The squash. */
        SQUASH,
        /** The rebase. */
        REBASE
    }

    /**
     * The status of auto merging a {@linkplain GHPullRequest}.
     *
     */
    @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Field comes from JSON deserialization")
    public static class AutoMerge {

        private GHUser enabled_by;
        private MergeMethod merge_method;
        private String commit_title;
        private String commit_message;

        /**
         * The user who enabled the auto merge of the pull request.
         *
         * @return the {@linkplain GHUser}
         */
        @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
        public GHUser getEnabledBy() {
            return enabled_by;
        }

        /**
         * The merge method of the auto merge.
         *
         * @return the {@linkplain MergeMethod}
         */
        public MergeMethod getMergeMethod() {
            return merge_method;
        }

        /**
         * the title of the commit, if e.g. {@linkplain MergeMethod#SQUASH} is used for the auto merge.
         *
         * @return the title of the commit
         */
        public String getCommitTitle() {
            return commit_title;
        }

        /**
         * the message of the commit, if e.g. {@linkplain MergeMethod#SQUASH} is used for the auto merge.
         *
         * @return the message of the commit
         */
        public String getCommitMessage() {
            return commit_message;
        }
    }
}
