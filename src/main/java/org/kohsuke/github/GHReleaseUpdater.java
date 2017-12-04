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

/**
 * Modifies {@link GHRelease}.
 *
 * @author Kohsuke Kawaguchi
 * @see GHRelease#update()
 */
public class GHReleaseUpdater {
    private final GHRelease base;
    private final Requester builder;

    GHReleaseUpdater(GHRelease base) {
        this.base = base;
        this.builder = new Requester(base.root);
    }

    public GHReleaseUpdater tag(String tag) {
        builder.with("tag_name",tag);
        return this;
    }

    /**
     * @param body The release notes body.
     */
    public GHReleaseUpdater body(String body) {
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
    public GHReleaseUpdater commitish(String commitish) {
        builder.with("target_commitish", commitish);
        return this;
    }

    /**
     * Optional.
     *
     * @param draft {@code true} to create a draft (unpublished) release, {@code false} to create a published one.
     *                          Default is {@code false}.
     */
    public GHReleaseUpdater draft(boolean draft) {
        builder.with("draft", draft);
        return this;
    }

    /**
     * @param name the name of the release
     */
    public GHReleaseUpdater name(String name) {
        builder.with("name", name);
        return this;
    }

    /**
     * Optional
     *
     * @param prerelease {@code true} to identify the release as a prerelease. {@code false} to identify the release
     *                               as a full release. Default is {@code false}.
     */
    public GHReleaseUpdater prerelease(boolean prerelease) {
        builder.with("prerelease", prerelease);
        return this;
    }

    public GHRelease update() throws IOException {
        return builder
                .method("PATCH")
                .to(base.owner.getApiTailUrl("releases/"+base.id), GHRelease.class).wrap(base.owner);
    }

}
