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

import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.IOException;

public class GHDeployKey {

    protected String url, key, title;
    protected boolean verified;
    protected int id;
    private GHRepository owner;

    public int getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public boolean isVerified() {
        return verified;
    }

    public GHDeployKey wrap(GHRepository repo) {
        this.owner = repo;
        return this;
    }

    public String toString() {
        return new ToStringBuilder(this).append("title",title).append("id",id).append("key",key).toString();
    }
    
    public void delete() throws IOException {
        new Requester(owner.root).method("DELETE").to(String.format("/repos/%s/%s/keys/%d", owner.getOwnerName(), owner.getName(), id));
    }
}
