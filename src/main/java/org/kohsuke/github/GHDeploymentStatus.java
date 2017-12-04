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

import java.net.URL;
import java.util.Locale;

public class GHDeploymentStatus extends GHObject {
    private GHRepository owner;
    private GitHub root;
    protected GHUser creator;
    protected String state;
    protected String description;
    protected String target_url;
    protected String deployment_url;
    protected String repository_url;
    public GHDeploymentStatus wrap(GHRepository owner) {
        this.owner = owner;
        this.root = owner.root;
        if(creator != null) creator.wrapUp(root);
        return this;
    }
    public URL getTargetUrl() {
        return GitHub.parseURL(target_url);
    }

    public URL getDeploymentUrl() {
        return GitHub.parseURL(deployment_url);
    }

    public URL getRepositoryUrl() {
        return GitHub.parseURL(repository_url);
    }
    
    public GHDeploymentState getState() {
        return GHDeploymentState.valueOf(state.toUpperCase(Locale.ENGLISH));
    }

    /**
     * @deprecated This object has no HTML URL.
     */
    @Override
    public URL getHtmlUrl() {
        return null;
    }
}
