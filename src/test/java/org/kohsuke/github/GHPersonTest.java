package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author Martin van Zijl
 */
public class GHPersonTest extends AbstractGitHubWireMockTest {
    @Test
    public void testFieldsForOrganization() throws Exception {
        GHRepository repo = getRepository();
        GHUser owner = repo.getOwner();
        assertThat(owner.getType(), equalTo("Organization"));
        assertThat(owner.isSiteAdmin(), notNullValue());
    }

    @Test
    public void testFieldsForUser() throws Exception {
        GHUser user = gitHub.getUser("kohsuke2");
        assertThat(user.getType(), equalTo("User"));
        assertThat(user.isSiteAdmin(), notNullValue());
    }

    protected GHRepository getRepository() throws IOException {
        return getRepository(gitHub);
    }

    private GHRepository getRepository(GitHub gitHub) throws IOException {
        return gitHub.getOrganization("hub4j-test-org").getRepository("github-api");
    }
}
