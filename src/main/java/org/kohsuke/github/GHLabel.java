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
 * @author Kohsuke Kawaguchi
 * @see GHIssue#getLabels()
 * @see GHRepository#listLabels()
 */
public class GHLabel {
    private String url, name, color;
    private GHRepository repo;

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    /**
     * Color code without leading '#', such as 'f29513'
     */
    public String getColor() {
        return color;
    }

    /*package*/ GHLabel wrapUp(GHRepository repo) {
        this.repo = repo;
        return this;
    }

    public void delete() throws IOException {
        repo.root.retrieve().method("DELETE").to(url);
    }

    /**
     * @param newColor
     *      6-letter hex color code, like "f29513"
     */
    public void setColor(String newColor) throws IOException {
        repo.root.retrieve().method("PATCH").with("name", name).with("color", newColor).to(url);
    }
}
