package org.kohsuke.github;

import org.junit.Test;
import org.kohsuke.github.authorization.OrgAppInstallationAuthorizationProvider;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

public class GHAuthenticatedAppInstallationTest extends AbstractGHAppInstallationTest {

    @Override
    protected GitHubBuilder getGitHubBuilder() {
        OrgAppInstallationAuthorizationProvider provider = new OrgAppInstallationAuthorizationProvider("hub4j-test-org",
                jwtProvider1);
        return super.getGitHubBuilder().withAuthorizationProvider(provider);
    }

    @Test
    public void testListRepositoriesTwoRepos() throws IOException {
        GHAuthenticatedAppInstallation appInstallation = gitHub.getInstallation();

        List<GHRepository> repositories = appInstallation.listRepositories().toList();

        assertThat(repositories.size(), equalTo(2));
        assertThat(repositories.stream().map(GHRepository::getName).toArray(),
                arrayContainingInAnyOrder("empty", "test-readme"));
    }

}
