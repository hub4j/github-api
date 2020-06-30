package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;

/**
 * @author Martin van Zijl
 */
public class GHDeploymentTest extends AbstractGitHubWireMockTest {

    @Test
    public void testGetDeploymentById() throws IOException {
        GHRepository repo = getRepository();
        GHDeployment deployment = repo.getDeployment(178653229);
        assertNotNull(deployment);
    }

    protected GHRepository getRepository() throws IOException {
        return getRepository(gitHub);
    }

    private GHRepository getRepository(GitHub gitHub) throws IOException {
        return gitHub.getOrganization("hub4j-test-org").getRepository("github-api");
    }
}
