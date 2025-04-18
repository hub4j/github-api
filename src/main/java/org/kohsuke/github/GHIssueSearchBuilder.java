package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

// TODO: Auto-generated Javadoc
/**
 * Search issues.
 *
 * @author Kohsuke Kawaguchi
 * @see GitHub#searchIssues() GitHub#searchIssues()
 */
public class GHIssueSearchBuilder extends GHSearchBuilder<GHIssue> {

    /**
     * The enum Sort.
     */
    public enum Sort {

        /** The comments. */
        COMMENTS,
        /** The created. */
        CREATED,
        /** The updated. */
        UPDATED
    }

    @SuppressFBWarnings(
            value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD", "NP_UNWRITTEN_FIELD" },
            justification = "JSON API")
    private static class IssueSearchResult extends SearchResult<GHIssue> {
        private GHIssue[] items;

        @Override
        GHIssue[] getItems(GitHub root) {
            for (GHIssue i : items) {
            }
            return items;
        }
    }

    /**
     * Instantiates a new GH issue search builder.
     *
     * @param root
     *            the root
     */
    GHIssueSearchBuilder(GitHub root) {
        super(root, IssueSearchResult.class);
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
     * Is open gh issue search builder.
     *
     * @return the gh issue search builder
     */
    public GHIssueSearchBuilder isOpen() {
        return q("is:open");
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
     * Search terms.
     *
     * @param term
     *            the term
     * @return the GH issue search builder
     */
    public GHIssueSearchBuilder q(String term) {
        super.q(term);
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
     * Gets the api url.
     *
     * @return the api url
     */
    @Override
    protected String getApiUrl() {
        return "/search/issues";
    }
}
