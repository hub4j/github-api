/*
 * GitHub API for Java
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.kohsuke.github;

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

    public GHIssueSearchBuilder order(GHDirection v) {
        req.with("order",v);
        return this;
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
