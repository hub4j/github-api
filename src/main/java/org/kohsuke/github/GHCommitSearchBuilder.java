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

import org.apache.commons.lang.StringUtils;

import java.io.IOException;

/**
 * Search commits.
 *
 * @author Marc de Verdelhan
 * @see GitHub#searchCommits()
 */
@Preview @Deprecated
public class GHCommitSearchBuilder extends GHSearchBuilder<GHCommit> {
    /*package*/ GHCommitSearchBuilder(GitHub root) {
        super(root,CommitSearchResult.class);
        req.withPreview(Previews.CLOAK);
    }

    /**
     * Search terms.
     */
    public GHCommitSearchBuilder q(String term) {
        super.q(term);
        return this;
    }

    public GHCommitSearchBuilder author(String v) {
        return q("author:"+v);
    }

    public GHCommitSearchBuilder committer(String v) {
        return q("committer:"+v);
    }

    public GHCommitSearchBuilder authorName(String v) {
        return q("author-name:"+v);
    }

    public GHCommitSearchBuilder committerName(String v) {
        return q("committer-name:"+v);
    }

    public GHCommitSearchBuilder authorEmail(String v) {
        return q("author-email:"+v);
    }

    public GHCommitSearchBuilder committerEmail(String v) {
        return q("committer-email:"+v);
    }

    public GHCommitSearchBuilder authorDate(String v) {
        return q("author-date:"+v);
    }

    public GHCommitSearchBuilder committerDate(String v) {
        return q("committer-date:"+v);
    }

    public GHCommitSearchBuilder merge(boolean merge) {
        return q("merge:"+Boolean.valueOf(merge).toString().toLowerCase());
    }

    public GHCommitSearchBuilder hash(String v) {
        return q("hash:"+v);
    }

    public GHCommitSearchBuilder parent(String v) {
        return q("parent:"+v);
    }

    public GHCommitSearchBuilder tree(String v) {
        return q("tree:"+v);
    }

    public GHCommitSearchBuilder is(String v) {
        return q("is:"+v);
    }

    public GHCommitSearchBuilder user(String v) {
        return q("user:"+v);
    }

    public GHCommitSearchBuilder org(String v) {
        return q("org:"+v);
    }

    public GHCommitSearchBuilder repo(String v) {
        return q("repo:"+v);
    }

    public GHCommitSearchBuilder order(GHDirection v) {
        req.with("order",v);
        return this;
    }

    public GHCommitSearchBuilder sort(Sort sort) {
        req.with("sort",sort);
        return this;
    }

    public enum Sort { AUTHOR_DATE, COMMITTER_DATE }

    private static class CommitSearchResult extends SearchResult<GHCommit> {
        private GHCommit[] items;

        @Override
        /*package*/ GHCommit[] getItems(GitHub root) {
            for (GHCommit commit : items) {
                String repoName = getRepoName(commit.url);
                try {
                    GHRepository repo = root.getRepository(repoName);
                    commit.wrapUp(repo);
                } catch (IOException ioe) {}
            }
            return items;
        }
    }
    
    /**
     * @param commitUrl a commit URL
     * @return the repo name ("username/reponame")
     */
    private static String getRepoName(String commitUrl) {
        if (StringUtils.isBlank(commitUrl)) {
            return null;
        }
        int indexOfUsername = (GitHub.GITHUB_URL + "/repos/").length();
        String[] tokens = commitUrl.substring(indexOfUsername).split("/", 3);
        return tokens[0] + '/' + tokens[1];
    }

    @Override
    protected String getApiUrl() {
        return "/search/commits";
    }
}
