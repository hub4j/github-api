package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import static org.hamcrest.Matchers.*;

// TODO: Auto-generated Javadoc
/**
 * The Class GHDeploymentTest.
 *
 * @author Martin van Zijl
 */
public class GHDeploymentTest extends AbstractGitHubWireMockTest {

    /**
     * Test get deployment by id string payload.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testGetDeploymentByIdStringPayload() throws IOException {
        final GHRepository repo = getRepository();
        final GHDeployment deployment = repo.getDeployment(178653229);
        assertThat(deployment, notNullValue());
        assertThat(deployment.getId(), equalTo(178653229L));
        assertThat(deployment.getEnvironment(), equalTo("production"));
        assertThat(deployment.getPayload(), equalTo("custom"));
        assertThat(deployment.getPayloadObject(), equalTo("custom"));
        assertThat(deployment.getRef(), equalTo("main"));
        assertThat(deployment.getSha(), equalTo("3a09d2de4a9a1322a0ba2c3e2f54a919ca8fe353"));
        assertThat(deployment.getTask(), equalTo("deploy"));
        assertThat(deployment.getOriginalEnvironment(), equalTo("production"));
        assertThat(deployment.isProductionEnvironment(), equalTo(false));
        assertThat(deployment.isTransientEnvironment(), equalTo(true));
    }

    /**
     * Test get deployment by id object payload.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testGetDeploymentByIdObjectPayload() throws IOException {
        final GHRepository repo = getRepository();
        final GHDeployment deployment = repo.getDeployment(178653229);
        assertThat(deployment, notNullValue());
        assertThat(deployment.getId(), equalTo(178653229L));
        assertThat(deployment.getEnvironment(), equalTo("production"));
        assertThat(deployment.getRef(), equalTo("main"));
        assertThat(deployment.getSha(), equalTo("3a09d2de4a9a1322a0ba2c3e2f54a919ca8fe353"));
        assertThat(deployment.getTask(), equalTo("deploy"));
        final Map<String, Object> payload = deployment.getPayloadMap();
        assertThat(payload.size(), equalTo(4));
        assertThat(payload.get("custom1"), equalTo(1));
        assertThat(payload.get("custom2"), equalTo("two"));
        assertThat(payload.get("custom3"), equalTo(Arrays.asList("3", 3, "three")));
        assertThat(payload.get("custom4"), nullValue());
        assertThat(deployment.getOriginalEnvironment(), equalTo("production"));
        assertThat(deployment.isProductionEnvironment(), equalTo(false));
        assertThat(deployment.isTransientEnvironment(), equalTo(true));
    }

    /**
     * Gets the repository.
     *
     * @return the repository
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected GHRepository getRepository() throws IOException {
        return getRepository(gitHub);
    }

    private GHRepository getRepository(final GitHub gitHub) throws IOException {
        return gitHub.getOrganization("hub4j-test-org").getRepository("github-api");
    }
}
