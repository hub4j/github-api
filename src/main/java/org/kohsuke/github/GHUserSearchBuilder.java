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
 * Search users.
 *
 * @author Kohsuke Kawaguchi
 * @see GitHub#searchUsers()
 */
public class GHUserSearchBuilder extends GHSearchBuilder<GHUser> {
    /*package*/ GHUserSearchBuilder(GitHub root) {
        super(root,UserSearchResult.class);
    }

    /**
     * Search terms.
     */
    public GHUserSearchBuilder q(String term) {
        super.q(term);
        return this;
    }

    public GHUserSearchBuilder type(String v) {
        return q("type:"+v);
    }

    public GHUserSearchBuilder in(String v) {
        return q("in:"+v);
    }

    public GHUserSearchBuilder repos(String v) {
        return q("repos:"+v);
    }

    public GHUserSearchBuilder location(String v) {
        return q("location:"+v);
    }

    public GHUserSearchBuilder language(String v) {
        return q("language:"+v);
    }

    public GHUserSearchBuilder created(String v) {
        return q("created:"+v);
    }

    public GHUserSearchBuilder followers(String v) {
        return q("followers:"+v);
    }

    public GHUserSearchBuilder order(GHDirection v) {
        req.with("order",v);
        return this;
    }

    public GHUserSearchBuilder sort(Sort sort) {
        req.with("sort",sort);
        return this;
    }

    public enum Sort { FOLLOWERS, REPOSITORIES, JOINED }

    private static class UserSearchResult extends SearchResult<GHUser> {
        private GHUser[] items;

        @Override
        /*package*/ GHUser[] getItems(GitHub root) {
            return GHUser.wrap(items,root);
        }
    }

    @Override
    protected String getApiUrl() {
        return "/search/users";
    }
}
