package org.kohsuke.github;

import org.junit.Test;
import org.kohsuke.github.authorization.AppInstallationAuthorizationProvider;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

// TODO: Auto-generated Javadoc
/**
 * The Class GHAuthenticatedAppInstallationTest.
 */
public class GHAuthenticatedAppInstallationTest extends AbstractGHAppInstallationTest {

    /**
     * Gets the git hub builder.
     *
     * @return the git hub builder
     */
    @Override
    protected GitHubBuilder getGitHubBuilder() {
        AppInstallationAuthorizationProvider provider = new AppInstallationAuthorizationProvider(
                app -> app.getInstallationByOrganization("hub4j-test-org"),
                jwtProvider1);
        return super.getGitHubBuilder().withAuthorizationProvider(provider);
    }

    /**
     * Test list repositories two repos.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testListRepositoriesTwoRepos() throws IOException {
        GHAuthenticatedAppInstallation appInstallation = gitHub.getInstallation();

        List<GHRepository> repositories = appInstallation.listRepositories().toList();

        assertThat(repositories.size(), equalTo(2));
        assertThat(repositories.stream().map(GHRepository::getName).toArray(),
                arrayContainingInAnyOrder("empty", "test-readme"));
    }

}
