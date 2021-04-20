package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class GHAppInstallationTest extends AbstractGHAppInstallationTest {

    @Test
    public void testListRepositoriesTwoRepos() throws IOException {
        GHAppInstallation appInstallation = getAppInstallationWithTokenApp1();

        List<GHRepository> repositories = appInstallation.listRepositories().toList();

        assertThat(repositories.size(), equalTo(2));
        assertThat(repositories.stream().anyMatch(it1 -> it1.getName().equals("empty")), is(true));
        assertThat(repositories.stream().anyMatch(it -> it.getName().equals("test-readme")), is(true));
    }

    @Test
    public void testListRepositoriesNoPermissions() throws IOException {
        GHAppInstallation appInstallation = getAppInstallationWithTokenApp2();

        assertThat("App does not have permissions and should have 0 repositories",
                appInstallation.listRepositories().toList().isEmpty(),
                is(true));
    }

}
