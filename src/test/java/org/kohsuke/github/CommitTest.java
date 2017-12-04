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

import com.google.common.collect.Iterables;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 */
public class CommitTest extends AbstractGitHubApiTestBase {
    @Test // issue 152
    public void lastStatus() throws IOException {
        GHTag t = gitHub.getRepository("stapler/stapler").listTags().iterator().next();
        t.getCommit().getLastStatus();
    }

    @Test // issue 230
    public void listFiles() throws Exception {
        GHRepository repo = gitHub.getRepository("stapler/stapler");
        PagedIterable<GHCommit> commits = repo.queryCommits().path("pom.xml").list();
        for (GHCommit commit : Iterables.limit(commits, 10)) {
            GHCommit expected = repo.getCommit( commit.getSHA1() );
            assertEquals(expected.getFiles().size(), commit.getFiles().size());
        }
    }
}
