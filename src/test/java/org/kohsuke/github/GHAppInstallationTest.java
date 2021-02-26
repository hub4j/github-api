package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class GHAppInstallationTest extends AbstractGHAppInstallationTest {

    @Test
    public void testListRepositoriesTwoRepos() throws IOException {
        GHAppInstallation appInstallation = getAppInstallationWithTokenApp1();

        List<GHRepository> repositories = appInstallation.listRepositories().toList();

        assertEquals(2, repositories.size());
        assertTrue(repositories.stream().anyMatch(it -> it.getName().equals("empty")));
        assertTrue(repositories.stream().anyMatch(it -> it.getName().equals("test-readme")));
    }

    @Test
    public void testListRepositoriesNoPermissions() throws IOException {
        GHAppInstallation appInstallation = getAppInstallationWithTokenApp2();

        assertTrue("App does not have permissions and should have 0 repositories",
                appInstallation.listRepositories().toList().isEmpty());
    }

}
