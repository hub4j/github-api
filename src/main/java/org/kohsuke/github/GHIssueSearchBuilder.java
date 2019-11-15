package org.kohsuke.github;

/**
 * Search issues.
 *
 * @author Kohsuke Kawaguchi
 * @see GitHub#searchIssues() GitHub#searchIssues()
 */
public class GHIssueSearchBuilder extends GHSearchBuilder<GHIssue> {
    GHIssueSearchBuilder(GitHub root) {
        super(root, IssueSearchResult.class);
    }

    /**
     * Search terms.
     */
    public GHIssueSearchBuilder q(String term) {
        super.q(term);
        return this;
    }

    /**
     * Mentions gh issue search builder.
     *
     * @param u
     *            the u
     * @return the gh issue search builder
     */
    public GHIssueSearchBuilder mentions(GHUser u) {
        return mentions(u.getLogin());
    }

    /**
     * Mentions gh issue search builder.
     *
     * @param login
     *            the login
     * @return the gh issue search builder
     */
    public GHIssueSearchBuilder mentions(String login) {
        return q("mentions:" + login);
    }

    /**
     * Is open gh issue search builder.
     *
     * @return the gh issue search builder
     */
    public GHIssueSearchBuilder isOpen() {
        return q("is:open");
    }

    /**
     * Is closed gh issue search builder.
     *
     * @return the gh issue search builder
     */
    public GHIssueSearchBuilder isClosed() {
        return q("is:closed");
    }

    /**
     * Is merged gh issue search builder.
     *
     * @return the gh issue search builder
     */
    public GHIssueSearchBuilder isMerged() {
        return q("is:merged");
    }

    /**
     * Order gh issue search builder.
     *
     * @param v
     *            the v
     * @return the gh issue search builder
     */
    public GHIssueSearchBuilder order(GHDirection v) {
        req.with("order", v);
        return this;
    }

    /**
     * Sort gh issue search builder.
     *
     * @param sort
     *            the sort
     * @return the gh issue search builder
     */
    public GHIssueSearchBuilder sort(Sort sort) {
        req.with("sort", sort);
        return this;
    }

    /**
     * The enum Sort.
     */
    public enum Sort {
        COMMENTS, CREATED, UPDATED
    }

    private static class IssueSearchResult extends SearchResult<GHIssue> {
        private GHIssue[] items;

        @Override
        GHIssue[] getItems(GitHub root) {
            for (GHIssue i : items)
                i.wrap(root);
            return items;
        }
    }

    @Override
    protected String getApiUrl() {
        return "/search/issues";
    }
}
