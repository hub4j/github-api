package org.kohsuke.github;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Search issues.
 *
 * @author Kohsuke Kawaguchi
 * @see GitHub#searchIssues()
 */
public class GHIssueSearchBuilder {
    private final GitHub root;
    private final Requester req;
    private final List<String> terms = new ArrayList<String>();

    /*package*/ GHIssueSearchBuilder(GitHub root) {
        this.root = root;
        req = root.retrieve();
    }

    /**
     * Search terms.
     */
    public GHIssueSearchBuilder q(String term) {
        terms.add(term);
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
        req.with("sort",sort.toString().toLowerCase(Locale.ENGLISH));
        return this;
    }

    public enum Sort { COMMENTS, CREATED, UPDATED }

    private static class IssueSearchResult extends SearchResult<GHIssue> {
        private GHIssue[] items;

        @Override
        public GHIssue[] getItems() {
            return items;
        }
    }

    /**
     * Lists up the issues with the criteria built so far.
     */
    public PagedSearchIterable<GHIssue> list() {
        return new PagedSearchIterable<GHIssue>() {
            public PagedIterator<GHIssue> iterator() {
                req.set("q", StringUtils.join(terms," "));
                return new PagedIterator<GHIssue>(adapt(req.asIterator("/search/issues", IssueSearchResult.class))) {
                    protected void wrapUp(GHIssue[] page) {
                        for (GHIssue c : page)
                            c.wrap(root);
                    }
                };
            }
        };
    }
}
