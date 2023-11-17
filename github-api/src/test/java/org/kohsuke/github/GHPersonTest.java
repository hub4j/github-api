package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

// TODO: Auto-generated Javadoc
/**
 * The Class GHPersonTest.
 *
 * @author Martin van Zijl
 */
public class GHPersonTest extends AbstractGitHubWireMockTest {

    /**
     * Test fields for organization.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testFieldsForOrganization() throws Exception {
        GHRepository repo = getRepository();
        GHUser owner = repo.getOwner();
        assertThat(owner.getType(), equalTo("Organization"));
        assertThat(owner.isSiteAdmin(), notNullValue());
    }

    /**
     * Test fields for user.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testFieldsForUser() throws Exception {
        GHUser user = gitHub.getUser("kohsuke2");
        assertThat(user.getType(), equalTo("User"));
        assertThat(user.isSiteAdmin(), notNullValue());
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

    private GHRepository getRepository(GitHub gitHub) throws IOException {
        return gitHub.getOrganization("hub4j-test-org").getRepository("github-api");
    }
}
