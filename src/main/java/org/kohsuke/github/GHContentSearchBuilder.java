/*
 * GitHub API for Java
 * Copyright (C) 2009-2017 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
 * Search code for {@link GHContent}.
 *
 * @author Kohsuke Kawaguchi
 * @see GitHub#searchContent()
 */
public class GHContentSearchBuilder extends GHSearchBuilder<GHContent> {
    /*package*/ GHContentSearchBuilder(GitHub root) {
        super(root,ContentSearchResult.class);
    }

    /**
     * Search terms.
     */
    public GHContentSearchBuilder q(String term) {
        super.q(term);
        return this;
    }

    public GHContentSearchBuilder in(String v) {
        return q("in:"+v);
    }

    public GHContentSearchBuilder language(String v) {
        return q("language:"+v);
    }

    public GHContentSearchBuilder fork(String v) {
        return q("fork:"+v);
    }

    public GHContentSearchBuilder size(String v) {
        return q("size:"+v);
    }

    public GHContentSearchBuilder path(String v) {
        return q("path:"+v);
    }

    public GHContentSearchBuilder filename(String v) {
        return q("filename:"+v);
    }

    public GHContentSearchBuilder extension(String v) {
        return q("extension:"+v);
    }

    public GHContentSearchBuilder user(String v) {
        return q("user:"+v);
    }


    public GHContentSearchBuilder repo(String v) {
        return q("repo:"+v);
    }

    private static class ContentSearchResult extends SearchResult<GHContent> {
        private GHContent[] items;

        @Override
        /*package*/ GHContent[] getItems(GitHub root) {
            for (GHContent item : items)
                item.wrap(root);
            return items;
        }
    }

    @Override
    protected String getApiUrl() {
        return "/search/code";
    }
}
