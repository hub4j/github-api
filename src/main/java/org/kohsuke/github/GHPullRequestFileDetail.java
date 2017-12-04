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

/**
 * File detail inside a {@link GHPullRequest}.
 * 
 * @author Julien Henry
 * @see GHPullRequest#listFiles()
 */
public class GHPullRequestFileDetail {

    String sha;
    String filename;
    String status;
    int additions;
    int deletions;
    int changes;
    String blob_url;
    String raw_url;
    String contents_url;
    String patch;

    public String getSha() {
        return sha;
    }

    public String getFilename() {
        return filename;
    }

    public String getStatus() {
        return status;
    }

    public int getAdditions() {
        return additions;
    }

    public int getDeletions() {
        return deletions;
    }

    public int getChanges() {
        return changes;
    }

    public URL getBlobUrl() {
        return GitHub.parseURL(blob_url);
    }

    public URL getRawUrl() {
        return GitHub.parseURL(raw_url);
    }

    public URL getContentsUrl() {
        return GitHub.parseURL(contents_url);
    }

    public String getPatch() {
        return patch;
    }
}
