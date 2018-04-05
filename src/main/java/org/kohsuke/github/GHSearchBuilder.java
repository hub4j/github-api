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

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for various search builders.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class GHSearchBuilder<T> extends GHQueryBuilder<T> {
    protected final List<String> terms = new ArrayList<String>();

    /**
     * Data transfer object that receives the result of search.
     */
    private final Class<? extends SearchResult<T>> receiverType;

    /*package*/ GHSearchBuilder(GitHub root, Class<? extends SearchResult<T>> receiverType) {
        super(root);
        this.receiverType = receiverType;
    }

    /**
     * Search terms.
     */
    public GHQueryBuilder<T> q(String term) {
        terms.add(term);
        return this;
    }

    /**
     * Performs the search.
     */
    @Override
    public PagedSearchIterable<T> list() {
        return new PagedSearchIterable<T>(root) {
            public PagedIterator<T> _iterator(int pageSize) {
                req.set("q", StringUtils.join(terms, " "));
                return new PagedIterator<T>(adapt(req.asIterator(getApiUrl(), receiverType, pageSize))) {
                    protected void wrapUp(T[] page) {
                        // SearchResult.getItems() should do it
                    }
                };
            }
        };
    }

    protected abstract String getApiUrl();
}
