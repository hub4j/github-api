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

import java.io.IOException;

/**
 * Builder pattern for creating a {@link GHRelease}
 *
 * @see GHRepository#createRelease(String)
 */
public class GHReleaseBuilder {
    private final GHRepository repo;
    private final Requester builder;

    public GHReleaseBuilder(GHRepository ghRepository, String tag) {
        this.repo = ghRepository;
        this.builder = new Requester(repo.root);
        builder.with("tag_name", tag);
    }

    /**
     * @param body The release notes body.
     */
    public GHReleaseBuilder body(String body) {
        builder.with("body", body);
        return this;
    }

    /**
     * Specifies the commitish value that determines where the Git tag is created from. Can be any branch or
     * commit SHA.
     *
     * @param commitish Defaults to the repository’s default branch (usually "master"). Unused if the Git tag
     *                  already exists.
     */
    public GHReleaseBuilder commitish(String commitish) {
        builder.with("target_commitish", commitish);
        return this;
    }

    /**
     * Optional.
     *
     * @param draft {@code true} to create a draft (unpublished) release, {@code false} to create a published one.
     *                          Default is {@code false}.
     */
    public GHReleaseBuilder draft(boolean draft) {
        builder.with("draft", draft);
        return this;
    }

    /**
     * @param name the name of the release
     */
    public GHReleaseBuilder name(String name) {
        builder.with("name", name);
        return this;
    }

    /**
     * Optional
     *
     * @param prerelease {@code true} to identify the release as a prerelease. {@code false} to identify the release
     *                               as a full release. Default is {@code false}.
     */
    public GHReleaseBuilder prerelease(boolean prerelease) {
        builder.with("prerelease", prerelease);
        return this;
    }

    public GHRelease create() throws IOException {
        return builder.to(repo.getApiTailUrl("releases"), GHRelease.class).wrap(repo);
    }
}
