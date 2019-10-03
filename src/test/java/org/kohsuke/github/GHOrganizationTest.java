package org.kohsuke.github;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class GHOrganizationTest extends AbstractGitHubApiWireMockTest {

    public static final String GITHUB_API_TEST = "github-api-test";

    @Test
    public void testCreateRepository() throws IOException {
        cleanUp();

        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHRepository repository = org.createRepository(GITHUB_API_TEST,
            "a test repository used to test kohsuke's github-api", "http://github-api.kohsuke.org/", "Core Developers", true);
        Assert.assertNotNull(repository);
    }

    @Test
    public void testCreateRepositoryWithAutoInitialization() throws IOException {
        cleanUp();

        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHRepository repository = org.createRepository(GITHUB_API_TEST)
            .description("a test repository used to test kohsuke's github-api")
            .homepage("http://github-api.kohsuke.org/")
            .team(org.getTeamByName("Core Developers"))
            .autoInit(true).create();
        Assert.assertNotNull(repository);
        Assert.assertNotNull(repository.getReadme());
    }

    @After
    public void cleanUp() throws IOException {
        if (githubApi.isUseProxy()) {
            GHRepository repository = gitHubBeforeAfter.getOrganization(GITHUB_API_TEST_ORG).getRepository(GITHUB_API_TEST);
            if (repository != null) {
                repository.delete();
            }
        }
    }
}
