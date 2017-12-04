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
 * Represents a deployment
 *
 * @see <a href="https://developer.github.com/v3/repos/deployments/">documentation</a>
 * @see GHRepository#listDeployments(String, String, String, String)
 * @see GHRepository#getDeployment(long)
 */
public class GHDeployment extends GHObject {
    private GHRepository owner;
    private GitHub root;
    protected String sha;
    protected String ref;
    protected String task;
    protected Object payload;
    protected String environment;
    protected String description;
    protected String statuses_url;
    protected String repository_url;
    protected GHUser creator;


    GHDeployment wrap(GHRepository owner) {
        this.owner = owner;
        this.root = owner.root;
        if(creator != null) creator.wrapUp(root);
        return this;
    }

    public URL getStatusesUrl() {
        return GitHub.parseURL(statuses_url);
    }

    public URL getRepositoryUrl() {
        return GitHub.parseURL(repository_url);
    }

    public String getTask() {
        return task;
    }
    public String getPayload() {
        return (String) payload;
    }
    public String getEnvironment() {
        return environment;
    }
    public GHUser getCreator() throws IOException {
        return root.intern(creator);
    }
    public String getRef() {
        return ref;
    }
    public String getSha(){
        return sha;
    }

    /**
     * @deprecated This object has no HTML URL.
     */
    @Override
    public URL getHtmlUrl() {
        return null;
    }

    public GHDeploymentStatusBuilder createStatus(GHDeploymentState state) {
        return new GHDeploymentStatusBuilder(owner,id,state);
    }

    public PagedIterable<GHDeploymentStatus> listStatuses() {
        return new PagedIterable<GHDeploymentStatus>() {
            public PagedIterator<GHDeploymentStatus> _iterator(int pageSize) {
                return new PagedIterator<GHDeploymentStatus>(root.retrieve().asIterator(statuses_url, GHDeploymentStatus[].class, pageSize)) {
                    @Override
                    protected void wrapUp(GHDeploymentStatus[] page) {
                        for (GHDeploymentStatus c : page)
                            c.wrap(owner);
                    }
                };
            }
        };
    }

}
