package org.kohsuke.github;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class GHOrganizationTest extends AbstractGitHubApiTestBase {

    public static final String GITHUB_API_TEST = "github-api-test";
    private GHOrganization org;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        if (githubApi.isUseProxy()) {
            org = gitHub.getOrganization("github-api-test-org");
        }
    }

    @Test
    public void testCreateRepository() throws IOException {
        GHRepository repository = org.createRepository(GITHUB_API_TEST,
            "a test repository used to test kohsuke's github-api", "http://github-api.kohsuke.org/", "Core Developers", true);
        Assert.assertNotNull(repository);
    }

    @Test
    public void testCreateRepositoryWithAutoInitialization() throws IOException {
        GHRepository repository = org.createRepository(GITHUB_API_TEST)
            .description("a test repository used to test kohsuke's github-api")
            .homepage("http://github-api.kohsuke.org/")
            .team(org.getTeamByName("Core Developers"))
            .autoInit(true).create();
        Assert.assertNotNull(repository);
        Assert.assertNotNull(repository.getReadme());
    }

    @After
    public void cleanUp() throws Exception {
        if (githubApi.isUseProxy()) {
            GHRepository repository = org.getRepository(GITHUB_API_TEST);
            repository.delete();
        }
    }
}
