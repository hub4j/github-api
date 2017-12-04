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
import java.util.List;

//Based on https://developer.github.com/v3/repos/deployments/#create-a-deployment
public class GHDeploymentBuilder {
    private final GHRepository repo;
    private final Requester builder;

    public GHDeploymentBuilder(GHRepository repo) {
        this.repo = repo;
        this.builder = new Requester(repo.root);
    }

    public GHDeploymentBuilder(GHRepository repo, String ref) {
        this(repo);
        ref(ref);
    }

    public GHDeploymentBuilder ref(String branch) {
        builder.with("ref",branch);
        return this;
    }
    public GHDeploymentBuilder task(String task) {
        builder.with("task",task);
        return this;
    }
    public GHDeploymentBuilder autoMerge(boolean autoMerge) {
        builder.with("auto_merge",autoMerge);
        return this;
    }

    public GHDeploymentBuilder requiredContexts(List<String> requiredContexts) {
        builder.with("required_contexts",requiredContexts);
        return this;
    }
    public GHDeploymentBuilder payload(String payload) {
        builder.with("payload",payload);
        return this;
    }

    public GHDeploymentBuilder environment(String environment) {
        builder.with("environment",environment);
        return this;
    }
    public GHDeploymentBuilder description(String description) {
        builder.with("description",description);
        return this;
    }

    public GHDeployment create() throws IOException {
        return builder.to(repo.getApiTailUrl("deployments"),GHDeployment.class).wrap(repo);
    }
}
