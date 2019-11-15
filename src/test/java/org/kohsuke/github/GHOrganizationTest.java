package org.kohsuke.github;

import com.jcraft.jsch.IO;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class GHOrganizationTest extends AbstractGitHubWireMockTest {

    public static final String GITHUB_API_TEST = "github-api-test";
    public static final String TEAM_NAME_CREATE = "create-team-test";

    @Before
    @After
    public void cleanUpTeam() throws IOException {
        // Cleanup is only needed when proxying
        if (!mockGitHub.isUseProxy()) {
            return;
        }

        GHTeam team = gitHubBeforeAfter.getOrganization(GITHUB_API_TEST_ORG).getTeamByName(TEAM_NAME_CREATE);
        if (team != null) {
            team.delete();
        }
    }

    @Test
    public void testCreateRepository() throws IOException {
        cleanupRepository(GITHUB_API_TEST_ORG + '/' + GITHUB_API_TEST);

        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHRepository repository = org.createRepository(GITHUB_API_TEST,
                "a test repository used to test kohsuke's github-api", "http://github-api.kohsuke.org/",
                "Core Developers", true);
        Assert.assertNotNull(repository);
    }

    @Test
    public void testCreateRepositoryWithAutoInitialization() throws IOException {
        cleanupRepository(GITHUB_API_TEST_ORG + '/' + GITHUB_API_TEST);

        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHRepository repository = org.createRepository(GITHUB_API_TEST)
                .description("a test repository used to test kohsuke's github-api")
                .homepage("http://github-api.kohsuke.org/").team(org.getTeamByName("Core Developers")).autoInit(true)
                .create();
        Assert.assertNotNull(repository);
        Assert.assertNotNull(repository.getReadme());
    }

    @Test
    public void testInviteUser() throws IOException {
        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHUser user = gitHub.getUser("martinvanzijl2");

        // First remove the user
        if (org.hasMember(user)) {
            org.remove(user);
        }

        // Then invite the user again
        org.add(user, GHOrganization.Role.MEMBER);

        // Now the user has to accept the invitation
        // Can this be automated?
        // user.acceptInvitationTo(org); // ?

        // Check the invitation has worked.
        // assertTrue(org.hasMember(user));
    }

    @Test
    public void testCreateTeamWithRepoAccess() throws IOException {
        String REPO_NAME = "github-api";

        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHRepository repo = org.getRepository(REPO_NAME);

        // Create team with access to repository. Check access was granted.
        GHTeam team = org.createTeam(TEAM_NAME_CREATE, GHOrganization.Permission.PUSH, repo);
        Assert.assertTrue(team.getRepositories().containsKey(REPO_NAME));
    }
}
