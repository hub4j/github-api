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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Content of reactions.
 *
 * @author Kohsuke Kawaguchi
 * @see <a href="https://developer.github.com/v3/reactions/">API documentation</a>
 * @see GHReaction
 */
public enum ReactionContent {
    PLUS_ONE("+1"),
    MINUS_ONE("-1"),
    LAUGH("laugh"),
    CONFUSED("confused"),
    HEART("heart"),
    HOORAY("hooray");

    private final String content;

    ReactionContent(String content) {
        this.content = content;
    }

    @JsonValue
    public String getContent() {
        return content;
    }

    @JsonCreator
    public static ReactionContent forContent(String content) {
        for (ReactionContent c : ReactionContent.values()) {
            if (c.getContent().equals(content))
                return c;
        }
        return null;
    }
}
