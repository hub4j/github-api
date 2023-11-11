package org.kohsuke.github;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Search for pull requests by main search terms in order to narrow down search results.
 *
 * @author Konstantin Gromov
 * @see <a href="https://docs.github.com/en/search-github/searching-on-github/searching-issues-and-pull-requests">Search
 *      issues and PRs</a>
 */
public class GHPullRequestSearchBuilder extends GHSearchBuilder<GHPullRequest> {
    /**
     * Instantiates a new GH search builder.
     *
     * @param root
     *            the root
     */
    GHPullRequestSearchBuilder(GitHub root) {
        super(root, PullRequestSearchResult.class);
    }

    /**
     * Instantiates a new GH search builder from repository.
     *
     * @param repository
     *            the gh repository
     */
    GHPullRequestSearchBuilder(GHRepository repository) {
        super(repository.root(), PullRequestSearchResult.class);
        this.repo(repository);
    }

    /**
     * Repository gh pull request search builder.
     *
     * @param repository
     *            the repository
     * @return the gh pull request search builder
     */
    public GHPullRequestSearchBuilder repo(GHRepository repository) {
        q("repo", repository.getFullName());
        return this;
    }

    /**
     * Author gh pull request search builder.
     *
     * @param user
     *            the user as pr author
     * @return the gh pull request search builder
     */
    public GHPullRequestSearchBuilder author(GHUser user) {
        q("author", user.getLogin());
        return this;
    }

    /**
     * CreatedByMe gh pull request search builder.
     *
     * @return the gh pull request search builder
     */
    public GHPullRequestSearchBuilder createdByMe() {
        q("author:@me");
        return this;
    }

    /**
     * Assigned to gh pull request user.
     *
     * @param u
     *            the gh user
     * @return the gh pull request search builder
     */
    public GHPullRequestSearchBuilder assigned(GHUser u) {
        q("assignee", u.getLogin());
        return this;
    }

    /**
     * Mentions gh pull request search builder.
     *
     * @param u
     *            the gh user
     * @return the gh pull request search builder
     */
    public GHPullRequestSearchBuilder mentions(GHUser u) {
        q("mentions", u.getLogin());
        return this;
    }

    /**
     * Is open gh pull request search builder.
     *
     * @return the gh pull request search builder
     */
    public GHPullRequestSearchBuilder isOpen() {
        return q("is:open");
    }

    /**
     * Is closed gh pull request search builder.
     *
     * @return the gh pull request search builder
     */
    public GHPullRequestSearchBuilder isClosed() {
        return q("is:closed");
    }

    /**
     * Is merged gh pull request search builder.
     *
     * @return the gh pull request search builder
     */
    public GHPullRequestSearchBuilder isMerged() {
        return q("is:merged");
    }

    /**
     * Is draft gh pull request search builder.
     *
     * @return the gh pull request search builder
     */
    public GHPullRequestSearchBuilder isDraft() {
        return q("draft:true");
    }

    /**
     * Head gh pull request search builder.
     *
     * @param branch
     *            the head branch
     * @return the gh pull request search builder
     */
    public GHPullRequestSearchBuilder head(GHBranch branch) {
        q("head", branch.getName());
        return this;
    }

    /**
     * Base gh pull request search builder.
     *
     * @param branch
     *            the base branch
     * @return the gh pull request search builder
     */
    public GHPullRequestSearchBuilder base(GHBranch branch) {
        q("base", branch.getName());
        return this;
    }

    /**
     * Commit gh pull request search builder.
     *
     * @param sha
     *            the commit SHA
     * @return the gh pull request search builder
     */
    public GHPullRequestSearchBuilder commit(String sha) {
        q("SHA", sha);
        return this;
    }

    /**
     * Created gh pull request search builder.
     *
     * @param created
     *            the createdAt
     * @return the gh pull request search builder
     */
    public GHPullRequestSearchBuilder created(LocalDate created) {
        q("created", created.format(DateTimeFormatter.ISO_DATE));
        return this;
    }

    /**
     * CreatedBefore gh pull request search builder.
     *
     * @param created
     *            the createdAt
     * @param inclusive
     *            whether to include date
     * @return the gh pull request search builder
     */
    public GHPullRequestSearchBuilder createdBefore(LocalDate created, boolean inclusive) {
        String comparisonSign = inclusive ? "<=" : "<";
        q("created:" + comparisonSign + created.format(DateTimeFormatter.ISO_DATE));
        return this;
    }

    /**
     * CreatedAfter gh pull request search builder.
     *
     * @param created
     *            the createdAt
     * @param inclusive
     *            whether to include date
     * @return the gh pull request search builder
     */
    public GHPullRequestSearchBuilder createdAfter(LocalDate created, boolean inclusive) {
        String comparisonSign = inclusive ? ">=" : ">";
        q("created:" + comparisonSign + created.format(DateTimeFormatter.ISO_DATE));
        return this;
    }

    /**
     * Created gh pull request search builder.
     *
     * @param from
     *            the createdAt starting from
     * @param to
     *            the createdAt ending to
     * @return the gh pull request search builder
     */
    public GHPullRequestSearchBuilder created(LocalDate from, LocalDate to) {
        String createdRange = from.format(DateTimeFormatter.ISO_DATE) + ".." + to.format(DateTimeFormatter.ISO_DATE);
        q("created", createdRange);
        return this;
    }

    /**
     * Merged gh pull request search builder.
     *
     * @param merged
     *            the merged
     * @return the gh pull request search builder
     */
    public GHPullRequestSearchBuilder merged(LocalDate merged) {
        q("merged", merged.format(DateTimeFormatter.ISO_DATE));
        return this;
    }

    /**
     * MergedBefore gh pull request search builder.
     *
     * @param merged
     *            the merged
     * @param inclusive
     *            whether to include date
     * @return the gh pull request search builder
     */
    public GHPullRequestSearchBuilder mergedBefore(LocalDate merged, boolean inclusive) {
        String comparisonSign = inclusive ? "<=" : "<";
        q("merged:" + comparisonSign + merged.format(DateTimeFormatter.ISO_DATE));
        return this;
    }

    /**
     * MergedAfter gh pull request search builder.
     *
     * @param merged
     *            the merged
     * @param inclusive
     *            whether to include date
     * @return the gh pull request search builder
     */
    public GHPullRequestSearchBuilder mergedAfter(LocalDate merged, boolean inclusive) {
        String comparisonSign = inclusive ? ">=" : ">";
        q("merged:" + comparisonSign + merged.format(DateTimeFormatter.ISO_DATE));
        return this;
    }

    /**
     * Merged gh pull request search builder.
     *
     * @param from
     *            the merged starting from
     * @param to
     *            the merged ending to
     * @return the gh pull request search builder
     */
    public GHPullRequestSearchBuilder merged(LocalDate from, LocalDate to) {
        String mergedRange = from.format(DateTimeFormatter.ISO_DATE) + ".." + to.format(DateTimeFormatter.ISO_DATE);
        q("merged", mergedRange);
        return this;
    }

    /**
     * Closed gh pull request search builder.
     *
     * @param closed
     *            the closed
     * @return the gh pull request search builder
     */
    public GHPullRequestSearchBuilder closed(LocalDate closed) {
        q("closed", closed.format(DateTimeFormatter.ISO_DATE));
        return this;
    }

    /**
     * ClosedBefore gh pull request search builder.
     *
     * @param closed
     *            the closed
     * @param inclusive
     *            whether to include date
     * @return the gh pull request search builder
     */
    public GHPullRequestSearchBuilder closedBefore(LocalDate closed, boolean inclusive) {
        String comparisonSign = inclusive ? "<=" : "<";
        q("closed:" + comparisonSign + closed.format(DateTimeFormatter.ISO_DATE));
        return this;
    }

    /**
     * ClosedAfter gh pull request search builder.
     *
     * @param closed
     *            the closed
     * @param inclusive
     *            whether to include date
     * @return the gh pull request search builder
     */
    public GHPullRequestSearchBuilder closedAfter(LocalDate closed, boolean inclusive) {
        String comparisonSign = inclusive ? ">=" : ">";
        q("closed:" + comparisonSign + closed.format(DateTimeFormatter.ISO_DATE));
        return this;
    }

    /**
     * Closed gh pull request search builder.
     *
     * @param from
     *            the closed starting from
     * @param to
     *            the closed ending to
     * @return the gh pull request search builder
     */
    public GHPullRequestSearchBuilder closed(LocalDate from, LocalDate to) {
        String closedRange = from.format(DateTimeFormatter.ISO_DATE) + ".." + to.format(DateTimeFormatter.ISO_DATE);
        q("closed", closedRange);
        return this;
    }

    /**
     * Updated gh pull request search builder.
     *
     * @param updated
     *            the updated
     * @return the gh pull request search builder
     */
    public GHPullRequestSearchBuilder updated(LocalDate updated) {
        q("updated", updated.format(DateTimeFormatter.ISO_DATE));
        return this;
    }

    /**
     * UpdatedBefore gh pull request search builder.
     *
     * @param updated
     *            the updated
     * @param inclusive
     *            whether to include date
     * @return the gh pull request search builder
     */
    public GHPullRequestSearchBuilder updatedBefore(LocalDate updated, boolean inclusive) {
        String comparisonSign = inclusive ? "<=" : "<";
        q("updated:" + comparisonSign + updated.format(DateTimeFormatter.ISO_DATE));
        return this;
    }

    /**
     * UpdatedAfter gh pull request search builder.
     *
     * @param updated
     *            the updated
     * @param inclusive
     *            whether to include date
     * @return the gh pull request search builder
     */
    public GHPullRequestSearchBuilder updatedAfter(LocalDate updated, boolean inclusive) {
        String comparisonSign = inclusive ? ">=" : ">";
        q("updated:" + comparisonSign + updated.format(DateTimeFormatter.ISO_DATE));
        return this;
    }

    /**
     * Updated gh pull request search builder.
     *
     * @param from
     *            the updated starting from
     * @param to
     *            the updated ending to
     * @return the gh pull request search builder
     */
    public GHPullRequestSearchBuilder updated(LocalDate from, LocalDate to) {
        String updatedRange = from.format(DateTimeFormatter.ISO_DATE) + ".." + to.format(DateTimeFormatter.ISO_DATE);
        q("updated", updatedRange);
        return this;
    }

    /**
     * Label gh pull request search builder.
     *
     * @param label
     *            the label
     * @return the gh pull request search builder
     */
    public GHPullRequestSearchBuilder label(String label) {
        q("label", label);
        return this;
    }

    /**
     * Labels gh pull request search builder.
     *
     * @param labels
     *            the labels
     * @return the gh pull request search builder
     */
    public GHPullRequestSearchBuilder inLabels(Iterable<String> labels) {
        q("label", String.join(",", labels));
        return this;
    }

    /**
     * Title like search term
     *
     * @param title
     *            the title to be matched
     * @return the gh pull request search builder
     */
    public GHPullRequestSearchBuilder titleLike(String title) {
        q(title + " in:title");
        return this;
    }

    /**
     * Order gh pull request search builder.
     *
     * @param direction
     *            the direction
     * @return the gh pull request search builder
     */
    public GHPullRequestSearchBuilder order(GHDirection direction) {
        req.with("order", direction);
        return this;
    }

    /**
     * Sort gh pull request search builder.
     *
     * @param sort
     *            the sort
     * @return the gh pull request search builder
     */
    public GHPullRequestSearchBuilder sort(GHPullRequestSearchBuilder.Sort sort) {
        req.with("sort", sort);
        return this;
    }

    @Override
    public GHPullRequestSearchBuilder q(String term) {
        super.q(term);
        return this;
    }

    @Override
    public PagedSearchIterable<GHPullRequest> list() {
        this.q("is:pr");
        return super.list();
    }

    @Override
    protected String getApiUrl() {
        return "/search/issues";
    }

    /**
     * The sort order values.
     */
    public enum Sort {

        /** The comments. */
        COMMENTS,
        /** The created. */
        CREATED,
        /** The updated. */
        UPDATED,
        /** The relevance. */
        RELEVANCE

    }

    private static class PullRequestSearchResult extends SearchResult<GHPullRequest> {

        private GHPullRequest[] items;

        @Override
        GHPullRequest[] getItems(GitHub root) {
            return items;
        }
    }
}
