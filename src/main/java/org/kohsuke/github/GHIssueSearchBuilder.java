package org.kohsuke.github;

import java.util.Locale;

/**
 * Search issues.
 *
 * @author Kohsuke Kawaguchi
 * @see GitHub#searchIssues()
 */
public class GHIssueSearchBuilder extends GHSearchBuilder<GHIssue> {
    /*package*/ GHIssueSearchBuilder(GitHub root) {
        super(root,IssueSearchResult.class);
    }

    /**
     * Search terms.
     */
    public GHIssueSearchBuilder q(String term) {
        super.q(term);
        return this;
    }

    public GHIssueSearchBuilder mentions(GHUser u) {
        return mentions(u.getLogin());
    }

    public GHIssueSearchBuilder mentions(String login) {
        return q("mentions:"+login);
    }

    public GHIssueSearchBuilder isOpen() {
        return q("is:open");
    }

    public GHIssueSearchBuilder isClosed() {
        return q("is:closed");
    }

    public GHIssueSearchBuilder isMerged() {
        return q("is:merged");
    }

    public GHIssueSearchBuilder sort(Sort sort) {
        req.with("sort",sort);
        return this;
    }

    public enum Sort { COMMENTS, CREATED, UPDATED }

    private static class IssueSearchResult extends SearchResult<GHIssue> {
        private GHIssue[] items;

        @Override
        /*package*/ GHIssue[] getItems(GitHub root) {
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
