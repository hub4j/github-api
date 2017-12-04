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
import java.util.Collections;
import java.util.LinkedHashMap;

/**
 * Builder pattern for creating a new Gist.
 *
 * @author Kohsuke Kawaguchi
 * @see GitHub#createGist()
 */
public class GHGistBuilder {
    private final GitHub root;
    private final Requester req;
    private final LinkedHashMap<String,Object> files = new LinkedHashMap<String, Object>();

    public GHGistBuilder(GitHub root) {
        this.root = root;
        req = new Requester(root);
    }

    public GHGistBuilder description(String desc) {
        req.with("description",desc);
        return this;
    }

    public GHGistBuilder public_(boolean v) {
        req.with("public",v);
        return this;
    }

    /**
     * Adds a new file.
     */
    public GHGistBuilder file(String fileName, String content) {
        files.put(fileName, Collections.singletonMap("content", content));
        return this;
    }

    /**
     * Creates a Gist based on the parameters specified thus far.
     */
    public GHGist create() throws IOException {
        req._with("files",files);
        return req.to("/gists",GHGist.class).wrapUp(root);
    }
}
