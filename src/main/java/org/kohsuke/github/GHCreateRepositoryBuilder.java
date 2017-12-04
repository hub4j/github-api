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

import java.io.IOException;
import java.net.URL;

/**
 * Creates a repository
 *
 * @author Kohsuke Kawaguchi
 */
public class GHCreateRepositoryBuilder {
    private final GitHub root;
    protected final Requester builder;
    private final String apiUrlTail;

    /*package*/ GHCreateRepositoryBuilder(GitHub root, String apiUrlTail, String name) {
        this.root = root;
        this.apiUrlTail = apiUrlTail;
        this.builder = new Requester(root);
        this.builder.with("name",name);
    }

    public GHCreateRepositoryBuilder description(String description) {
      this.builder.with("description",description);
      return this;
    }

    public GHCreateRepositoryBuilder homepage(URL homepage) {
        return homepage(homepage.toExternalForm());
    }

    public GHCreateRepositoryBuilder homepage(String homepage) {
        this.builder.with("homepage",homepage);
        return this;
    }

    /**
     * Creates a private repository
     */
    public GHCreateRepositoryBuilder private_(boolean b) {
        this.builder.with("private",b);
        return this;
    }

    /**
     * Enables issue tracker
     */
    public GHCreateRepositoryBuilder issues(boolean b) {
        this.builder.with("has_issues",b);
        return this;
    }

    /**
     * Enables wiki
     */
    public GHCreateRepositoryBuilder wiki(boolean b) {
        this.builder.with("has_wiki",b);
        return this;
    }

    /**
     * Enables downloads
     */
    public GHCreateRepositoryBuilder downloads(boolean b) {
        this.builder.with("has_downloads",b);
        return this;
    }

    /**
     * If true, create an initial commit with empty README.
     */
    public GHCreateRepositoryBuilder autoInit(boolean b) {
        this.builder.with("auto_init",b);
        return this;
    }

    /**
     * Creates a default .gitignore
     *
     * See https://developer.github.com/v3/repos/#create
     */
    public GHCreateRepositoryBuilder gitignoreTemplate(String language) {
        this.builder.with("gitignore_template",language);
        return this;
    }

    /**
     * Desired license template to apply
     *
     * See https://developer.github.com/v3/repos/#create
     */
    public GHCreateRepositoryBuilder licenseTemplate(String license) {
        this.builder.with("license_template",license);
        return this;
    }

    /**
     * The team that gets granted access to this repository. Only valid for creating a repository in
     * an organization.
     */
    public GHCreateRepositoryBuilder team(GHTeam team) {
        if (team!=null)
            this.builder.with("team_id",team.getId());
        return this;
    }

    /**
     * Creates a repository with all the parameters.
     */
    public GHRepository create() throws IOException {
        return builder.method("POST").to(apiUrlTail, GHRepository.class).wrap(root);
    }

}
