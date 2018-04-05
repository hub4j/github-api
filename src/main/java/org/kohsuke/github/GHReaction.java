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
import java.net.URL;

import static org.kohsuke.github.Previews.*;

/**
 * Reaction to issue, comment, PR, and so on.
 *
 * @author Kohsuke Kawaguchi
 * @see Reactable
 */
@Preview @Deprecated
public class GHReaction extends GHObject {
    private GitHub root;

    private GHUser user;
    private ReactionContent content;

    /*package*/ GHReaction wrap(GitHub root) {
        this.root = root;
        user.wrapUp(root);
        return this;
    }

    /**
     * The kind of reaction left.
     */
    public ReactionContent getContent() {
        return content;
    }

    /**
     * User who left the reaction.
     */
    public GHUser getUser() {
        return user;
    }

    /**
     * Reaction has no HTML URL. Don't call this method.
     */
    @Deprecated
    public URL getHtmlUrl() {
        return null;
    }

    /**
     * Removes this reaction.
     */
    public void delete() throws IOException {
        new Requester(root).method("DELETE").withPreview(SQUIRREL_GIRL).to("/reactions/"+id);
    }
}
