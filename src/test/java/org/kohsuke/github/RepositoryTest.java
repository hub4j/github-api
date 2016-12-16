package org.kohsuke.github;

import java.io.FileNotFoundException;
import org.junit.Test;
import org.kohsuke.github.GHRepository.Contributor;

import java.io.IOException;
import org.junit.Ignore;

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

    @Ignore("depends on who runs this test whether it can pass or not")
    @Test
    public void getPermission() throws Exception {
        GHRepository r = gitHub.getOrganization("cloudbeers").getRepository("yolo");
        assertEquals("admin", r.getPermission("jglick").getPermission());
        assertEquals("read", r.getPermission("dude").getPermission());
        r = gitHub.getOrganization("cloudbees").getRepository("private-repo-not-writable-by-me");
        try {
            r.getPermission("jglick");
            fail();
        } catch (FileNotFoundException x) {
            x.printStackTrace(); // good
        }
        r = gitHub.getOrganization("apache").getRepository("groovy");
        try {
            r.getPermission("jglick");
            fail();
        } catch (HttpException x) {
            x.printStackTrace(); // good
            assertEquals(403, x.getResponseCode());
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
