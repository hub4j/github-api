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

import javax.annotation.CheckForNull;
import java.io.FileNotFoundException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

/**
 * Request/responce contains useful metadata.
 * Custom exception allows store info for next diagnostics.
 *
 * @author Kanstantsin Shautsou
 */
public class GHFileNotFoundException extends FileNotFoundException {
    protected Map<String, List<String>> responseHeaderFields;

    public GHFileNotFoundException() {
    }

    public GHFileNotFoundException(String s) {
        super(s);
    }

    @CheckForNull
    public Map<String, List<String>> getResponseHeaderFields() {
        return responseHeaderFields;
    }

    GHFileNotFoundException withResponseHeaderFields(HttpURLConnection urlConnection) {
        this.responseHeaderFields = urlConnection.getHeaderFields();
        return this;
    }
}
