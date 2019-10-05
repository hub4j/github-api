package org.kohsuke.github;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeFalse;

/**
 * @author Liam Newman
 */
public class GHRepositoryTest extends AbstractGitHubWireMockTest {

    @Test
    public void archive() throws Exception {
        snapshotNotAllowed();

        // Archive is a one-way action in the API.
        // We do thi this one
        GHRepository repo = getRepository();

        assertThat(repo.isArchived(), is(false));

        repo.archive();

        assertThat(repo.isArchived(), is(true));
        assertThat(getRepository().isArchived(), is(true));
    }

    @Test
    public void getBranch_URLEncoded() throws Exception {
        GHRepository repo = getRepository();
        GHBranch branch = repo.getBranch("test/#UrlEncode");
        assertThat(branch.getName(), is("test/#UrlEncode"));
    }

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
    public void testSetPublic() throws Exception {
        kohsuke();
        GHUser myself = gitHub.getMyself();
        String repoName = "test-repo-public";
        GHRepository repo = gitHub.createRepository(repoName).private_(false).create();
        try {
            assertFalse(repo.isPrivate());
            repo.setPrivate(true);
            assertTrue(myself.getRepository(repoName).isPrivate());
            repo.setPrivate(false);
            assertFalse(myself.getRepository(repoName).isPrivate());
        } finally {
            repo.delete();
        }
    }

    @Test
    public void listContributors() throws IOException {
        GHRepository r = gitHub.getOrganization("github-api").getRepository("github-api");
        int i = 0;
        boolean kohsuke = false;

        for (GHRepository.Contributor c : r.listContributors()) {
            if (c.getLogin().equals("kohsuke")) {
                assertTrue(c.getContributions() > 0);
                kohsuke = true;
            }
            if (i++ > 5) {
                break;
            }
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
            //x.printStackTrace(); // good
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

    @Test
    public void listReleases() throws IOException {
        PagedIterable<GHRelease> releases = gitHub.getOrganization("github").getRepository("hub").listReleases();
        assertTrue(releases.iterator().hasNext());
    }

    @Test
    public void getReleaseExists() throws IOException {
        GHRelease release = gitHub.getOrganization("github").getRepository("hub").getRelease(6839710);
        assertEquals("v2.3.0-pre10", release.getTagName());
    }

    @Test
    public void getReleaseDoesNotExist() throws IOException {
        GHRelease release = gitHub.getOrganization("github").getRepository("hub").getRelease(Long.MAX_VALUE);
        assertNull(release);
    }

    @Test
    public void getReleaseByTagNameExists() throws IOException {
        GHRelease release = gitHub.getOrganization("github").getRepository("hub").getReleaseByTagName("v2.3.0-pre10");
        assertNotNull(release);
        assertEquals("v2.3.0-pre10", release.getTagName());
    }

    @Test
    public void getReleaseByTagNameDoesNotExist() throws IOException {
        GHRelease release = getRepository().getReleaseByTagName("foo-bar-baz");
        assertNull(release);
    }

    @Test
    public void listLanguages() throws IOException {
        GHRepository r = gitHub.getRepository("github-api/github-api");
        String mainLanguage = r.getLanguage();
        assertTrue(r.listLanguages().containsKey(mainLanguage));
    }

    @Test // Issue #261
    public void listEmptyContributors() throws IOException {
        for (GHRepository.Contributor c : gitHub.getRepository(GITHUB_API_TEST_ORG + "/empty").listContributors()) {
            System.out.println(c);
            fail("This list should be empty, but should return a valid empty iterable.");
        }
    }

    protected GHRepository getRepository() throws IOException {
        return getRepository(gitHub);
    }

    private GHRepository getRepository(GitHub gitHub) throws IOException {
        return gitHub.getOrganization("github-api-test-org").getRepository("github-api");
    }


}