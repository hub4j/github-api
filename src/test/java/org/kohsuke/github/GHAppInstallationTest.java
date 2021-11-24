package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.*;

public class GHAppInstallationTest extends AbstractGHAppInstallationTest {

    @Test
    public void testListRepositoriesTwoRepos() throws IOException {
        GHAppInstallation appInstallation = getAppInstallationWithToken(jwtProvider1.getEncodedAuthorization());

        List<GHRepository> repositories = appInstallation.listRepositories().toList();

        assertThat(repositories.size(), equalTo(2));
        assertThat(repositories.stream().map(GHRepository::getName).toArray(),
                arrayContainingInAnyOrder("empty", "test-readme"));
    }

    @Test
    public void testListRepositoriesNoPermissions() throws IOException {
        GHAppInstallation appInstallation = getAppInstallationWithToken(jwtProvider2.getEncodedAuthorization());

        assertThat("App does not have permissions and should have 0 repositories",
                appInstallation.listRepositories().toList().isEmpty());
    }

}
