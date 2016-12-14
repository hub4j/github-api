package org.kohsuke.github;

import org.junit.Test;
import org.kohsuke.github.GHRepository.Contributor;

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
