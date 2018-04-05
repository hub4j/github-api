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

import java.util.Locale;

/**
 * Rendering mode of markdown.
 *
 * @author Kohsuke Kawaguchi
 * @see GitHub#renderMarkdown(String)
 * @see GHRepository#renderMarkdown(String, MarkdownMode)
 */
public enum MarkdownMode {
    /**
     * Render a document as plain Markdown, just like README files are rendered.
     */
    MARKDOWN,
    /**
     * Render a document as user-content, e.g. like user comments or issues are rendered.
     * In GFM mode, hard line breaks are always taken into account, and issue and user
     * mentions are linked accordingly.
     *
     * @see GHRepository#renderMarkdown(String, MarkdownMode)
     */
    GFM;

    public String toString() {
        return name().toLowerCase(Locale.ENGLISH);
    }
}
