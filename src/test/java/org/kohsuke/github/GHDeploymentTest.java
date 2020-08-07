package org.kohsuke.github;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.junit.Test;

/**
 * @author Martin van Zijl
 */
public class GHDeploymentTest extends AbstractGitHubWireMockTest {

    @Test
    public void testGetDeploymentByIdStringPayload() throws IOException {
        final GHRepository repo = getRepository();
        final GHDeployment deployment = repo.getDeployment(178653229);
        assertNotNull(deployment);
        assertEquals(178653229, deployment.getId());
        assertEquals("production", deployment.getEnvironment());
        assertEquals("custom", deployment.getPayload());
        assertEquals("custom", deployment.getPayloadObject());
        assertEquals("master", deployment.getRef());
        assertEquals("3a09d2de4a9a1322a0ba2c3e2f54a919ca8fe353", deployment.getSha());
        assertEquals("deploy", deployment.getTask());
    }

    @Test
    public void testGetDeploymentByIdObjectPayload() throws IOException {
        final GHRepository repo = getRepository();
        final GHDeployment deployment = repo.getDeployment(178653229);
        assertNotNull(deployment);
        assertEquals(178653229, deployment.getId());
        assertEquals("production", deployment.getEnvironment());
        assertEquals("master", deployment.getRef());
        assertEquals("3a09d2de4a9a1322a0ba2c3e2f54a919ca8fe353", deployment.getSha());
        assertEquals("deploy", deployment.getTask());
        final Map<String, Object> payload = deployment.getPayloadMap();
        assertEquals(4, payload.size());
        assertEquals(1, payload.get("custom1"));
        assertEquals("two", payload.get("custom2"));
        assertEquals(Arrays.asList("3", 3, "three"), payload.get("custom3"));
        assertNull(payload.get("custom4"));
    }

    protected GHRepository getRepository() throws IOException {
        return getRepository(gitHub);
    }

    private GHRepository getRepository(final GitHub gitHub) throws IOException {
        return gitHub.getOrganization("hub4j-test-org").getRepository("github-api");
    }
}
