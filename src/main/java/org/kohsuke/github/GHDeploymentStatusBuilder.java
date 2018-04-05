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
 * Creates a new deployment status.
 *
 * @see
 *      GHDeployment#createStatus(GHDeploymentState)
 */
public class GHDeploymentStatusBuilder {
    private final Requester builder;
    private GHRepository repo;
    private long deploymentId;

    /**
     * @deprecated
     *      Use {@link GHDeployment#createStatus(GHDeploymentState)}
     */
    public GHDeploymentStatusBuilder(GHRepository repo, int deploymentId, GHDeploymentState state) {
        this(repo,(long)deploymentId,state);
    }

    /*package*/ GHDeploymentStatusBuilder(GHRepository repo, long deploymentId, GHDeploymentState state) {
        this.repo = repo;
        this.deploymentId = deploymentId;
        this.builder = new Requester(repo.root);
        this.builder.with("state",state);
    }

    public GHDeploymentStatusBuilder description(String description) {
      this.builder.with("description",description);
      return this;
    }

    public GHDeploymentStatusBuilder targetUrl(String targetUrl) {
        this.builder.with("target_url",targetUrl);
        return this;
    }

    public GHDeploymentStatus create() throws IOException {
        return builder.to(repo.getApiTailUrl("deployments/"+deploymentId+"/statuses"),GHDeploymentStatus.class).wrap(repo);
    }
}
