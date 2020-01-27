package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Martin van Zijl
 */
public class GHPersonTest extends AbstractGitHubWireMockTest {
    @Test
    public void testFieldsForOrganization() throws Exception {
        GHRepository repo = getRepository();
        GHUser owner = repo.getOwner();
        assertEquals("Organization", owner.getType());
        assertNotNull(owner.isSiteAdmin());
    }

    @Test
    public void testFieldsForUser() throws Exception {
        GHUser user = gitHub.getUser("kohsuke2");
        assertEquals("User", user.getType());
        assertNotNull(user.isSiteAdmin());
    }

    protected GHRepository getRepository() throws IOException {
        return getRepository(gitHub);
    }

    private GHRepository getRepository(GitHub gitHub) throws IOException {
        return gitHub.getOrganization("github-api-test-org").getRepository("github-api");
    }
}
