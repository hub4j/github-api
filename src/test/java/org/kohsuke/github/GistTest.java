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

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Kohsuke Kawaguchi
 */
@Ignore("ignored as out of scope of SonarSource's fork's changes")
public class GistTest extends AbstractGitHubApiTestBase {
    /**
     * CRUD operation.
     */
    @Test
    public void lifecycleTest() throws Exception {
        GHGist gist = gitHub.createGist()
                .public_(false)
                .description("Test Gist")
                .file("abc.txt","abc")
                .file("def.txt","def")
                .create();

        assertNotNull(gist.getCreatedAt());
        assertNotNull(gist.getUpdatedAt());

        assertNotNull(gist.getCommentsUrl());
        assertNotNull(gist.getCommitsUrl());
        assertNotNull(gist.getGitPullUrl());
        assertNotNull(gist.getGitPushUrl());
        assertNotNull(gist.getHtmlUrl());

        gist.delete();
    }

    @Test
    public void starTest() throws Exception {
        GHGist gist = gitHub.getGist("9903708");
        assertEquals("rtyler",gist.getOwner().getLogin());

        gist.star();
        assertTrue(gist.isStarred());
        gist.unstar();
        assertFalse(gist.isStarred());

        GHGist newGist = gist.fork();

        try {
            for (GHGist g : gist.listForks()) {
                if (g.equals(newGist)) {
                    // expected to find it in the clone list
                    return;
                }
            }

            fail("Expected to find a newly cloned gist");
        } finally {
            newGist.delete();
        }
    }

    @Test
    public void gistFile() throws Exception {
        GHGist gist = gitHub.getGist("9903708");

        assertTrue(gist.isPublic());

        assertEquals(1,gist.getFiles().size());
        GHGistFile f = gist.getFile("keybase.md");

        assertEquals("text/plain", f.getType());
        assertEquals("Markdown", f.getLanguage());
        assertTrue(f.getContent().contains("### Keybase proof"));
        assertNotNull(f.getContent());
    }
}
