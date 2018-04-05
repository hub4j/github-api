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

import org.junit.Test;
import org.kohsuke.github.GHRepository.Contributor;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 */
public class RepositoryTest extends AbstractGitHubApiTestBase {
    @Test
    public void subscription() throws Exception {
        GHRepository r = getRepository();
        assertNull(r.getSubscription());

        GHSubscription s = r.subscribe(true, false);
        assertEquals(s.getRepository(), r);

        s.delete();

        assertNull(r.getSubscription());
    }

    @Test
    public void listContributors() throws IOException {
        GHRepository r = gitHub.getOrganization("stapler").getRepository("stapler");
        int i=0;
        boolean kohsuke = false;

        for (Contributor c : r.listContributors()) {
            System.out.println(c.getName());
            assertTrue(c.getContributions()>0);
            if (c.getLogin().equals("kohsuke"))
                kohsuke = true;
            if (i++ > 5)
                break;
        }

        assertTrue(kohsuke);
    }

    @Test
    public void getPermission() throws Exception {
        kohsuke();
        GHRepository r = gitHub.getRepository("github-api-test-org/test-permission");
        assertEquals(GHPermissionType.ADMIN, r.getPermission("kohsuke"));
        assertEquals(GHPermissionType.READ, r.getPermission("dude"));
        r = gitHub.getOrganization("apache").getRepository("groovy");
        try {
            r.getPermission("jglick");
            fail();
        } catch (HttpException x) {
            x.printStackTrace(); // good
            assertEquals(403, x.getResponseCode());
        }

        if (false) {
            // can't easily test this; there's no private repository visible to the test user
            r = gitHub.getOrganization("cloudbees").getRepository("private-repo-not-writable-by-me");
            try {
                r.getPermission("jglick");
                fail();
            } catch (FileNotFoundException x) {
                x.printStackTrace(); // good
            }
        }
    }
    
    
	
	@Test
	public void LatestRepositoryExist() {
		try {
			// add the repository that have latest release
			GHRelease release = gitHub.getRepository("kamontat/CheckIDNumber").getLatestRelease();
			assertEquals("v3.0", release.getTagName());
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void LatestRepositoryNotExist() {
		try {
			// add the repository that `NOT` have latest release
			GHRelease release = gitHub.getRepository("kamontat/Java8Example").getLatestRelease();
			assertNull(release);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

    private GHRepository getRepository() throws IOException {
        return gitHub.getOrganization("github-api-test-org").getRepository("jenkins");
    }

    @Test
    public void listLanguages() throws IOException {
        GHRepository r = gitHub.getRepository("kohsuke/github-api");
        String mainLanguage = r.getLanguage();
        assertTrue(r.listLanguages().containsKey(mainLanguage));
    }

    @Test // Issue #261
    public void listEmptyContributors() throws IOException {
        GitHub gh = GitHub.connect();
        for (Contributor c : gh.getRepository("github-api-test-org/empty").listContributors()) {
            System.out.println(c);
        }
    }
}
