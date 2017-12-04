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
 * Lists up pull requests with some filtering and sorting.
 *
 * @author Kohsuke Kawaguchi
 * @see GHRepository#queryPullRequests()
 */
public class GHPullRequestQueryBuilder extends GHQueryBuilder<GHPullRequest> {
    private final GHRepository repo;

    /*package*/ GHPullRequestQueryBuilder(GHRepository repo) {
        super(repo.root);
        this.repo = repo;
    }

    public GHPullRequestQueryBuilder state(GHIssueState state) {
        req.with("state",state);
        return this;
    }

    public GHPullRequestQueryBuilder head(String head) {
        req.with("head",head);
        return this;
    }

    public GHPullRequestQueryBuilder base(String base) {
        req.with("base",base);
        return this;
    }

    public GHPullRequestQueryBuilder sort(Sort sort) {
        req.with("sort",sort);
        return this;
    }

    public enum Sort { CREATED, UPDATED, POPULARITY, LONG_RUNNING }

    public GHPullRequestQueryBuilder direction(GHDirection d) {
        req.with("direction",d);
        return this;
    }

    @Override
    public PagedIterable<GHPullRequest> list() {
        return new PagedIterable<GHPullRequest>() {
            public PagedIterator<GHPullRequest> _iterator(int pageSize) {
                return new PagedIterator<GHPullRequest>(req.asIterator(repo.getApiTailUrl("pulls"), GHPullRequest[].class, pageSize)) {
                    @Override
                    protected void wrapUp(GHPullRequest[] page) {
                        for (GHPullRequest pr : page)
                            pr.wrapUp(repo);
                    }
                };
            }
        };
    }
}
