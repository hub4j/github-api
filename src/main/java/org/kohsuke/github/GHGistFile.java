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

/**
 * A file inside {@link GHGist}
 *
 * @author Kohsuke Kawaguchi
 * @see GHGist#getFile(String)
 * @see GHGist#getFiles()
 */
public class GHGistFile {
    /*package almost final*/ String fileName;

    private int size;
    private String raw_url, type, language, content;
    private boolean truncated;


    public String getFileName() {
        return fileName;
    }

    /**
     * File size in bytes.
     */
    public int getSize() {
        return size;
    }

    /**
     * URL that serves this file as-is.
     */
    public String getRawUrl() {
        return raw_url;
    }

    /**
     * Content type of this Gist, such as "text/plain"
     */
    public String getType() {
        return type;
    }

    public String getLanguage() {
        return language;
    }

    /**
     * Content of this file.
     */
    public String getContent() {
        return content;
    }

    /**
     * (?) indicates if {@link #getContent()} contains a truncated content.
     */
    public boolean isTruncated() {
        return truncated;
    }
}
