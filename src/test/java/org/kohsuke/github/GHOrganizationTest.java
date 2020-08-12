package org.kohsuke.github;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.github.GHOrganization.Permission;

import java.io.IOException;
import java.util.List;

public class GHOrganizationTest extends AbstractGitHubWireMockTest {

    public static final String GITHUB_API_TEST = "github-api-test";
    public static final String GITHUB_API_TEMPLATE_TEST = "github-api-template-test";
    public static final String TEAM_NAME_CREATE = "create-team-test";

    @Before
    @After
    public void cleanUpTeam() throws IOException {
        // Cleanup is only needed when proxying
        if (!mockGitHub.isUseProxy()) {
            return;
        }

        GHTeam team = getGitHubBeforeAfter().getOrganization(GITHUB_API_TEST_ORG).getTeamByName(TEAM_NAME_CREATE);
        if (team != null) {
            team.delete();
        }
    }

    @Test
    public void testCreateRepository() throws IOException {
        cleanupRepository(GITHUB_API_TEST_ORG + '/' + GITHUB_API_TEST);

        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHRepository repository = org.createRepository(GITHUB_API_TEST,
                "a test repository used to test kohsuke's github-api",
                "http://github-api.kohsuke.org/",
                "Core Developers",
                true);
        Assert.assertNotNull(repository);
    }

    @Test
    public void testCreateRepositoryWithAutoInitialization() throws IOException {
        cleanupRepository(GITHUB_API_TEST_ORG + '/' + GITHUB_API_TEST);

        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHRepository repository = org.createRepository(GITHUB_API_TEST)
                .description("a test repository used to test kohsuke's github-api")
                .homepage("http://github-api.kohsuke.org/")
                .team(org.getTeamByName("Core Developers"))
                .autoInit(true)
                .create();
        Assert.assertNotNull(repository);
        Assert.assertNotNull(repository.getReadme());
    }

    @Test
    public void testCreateRepositoryWithParameterIsTemplate() throws IOException {
        cleanupRepository(GITHUB_API_TEST_ORG + '/' + GITHUB_API_TEST);

        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHRepository repository = org.createRepository(GITHUB_API_TEMPLATE_TEST)
                .description("a test template repository used to test kohsuke's github-api")
                .homepage("http://github-api.kohsuke.org/")
                .team(org.getTeamByName("Core Developers"))
                .autoInit(true)
                .templateRepository(true)
                .create();
        Assert.assertNotNull(repository);
        Assert.assertNotNull(repository.getReadme());
    }

    @Test
    public void testCreateRepositoryWithTemplate() throws IOException {
        cleanupRepository(GITHUB_API_TEST_ORG + '/' + GITHUB_API_TEST);

        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHRepository repository = org.createRepository(GITHUB_API_TEST)
                .fromTemplateRepository(GITHUB_API_TEST_ORG, GITHUB_API_TEMPLATE_TEST)
                .owner(GITHUB_API_TEST_ORG)
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
    public void testListMembersWithFilter() throws IOException {
        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);

        List<GHUser> admins = org.listMembersWithFilter("all").asList();

        assertNotNull(admins);
        assertTrue(admins.size() >= 12); // In case more are added in the future
        assertTrue(admins.stream().anyMatch(ghUser -> ghUser.getLogin().equals("alexanderrtaylor")));
        assertTrue(admins.stream().anyMatch(ghUser -> ghUser.getLogin().equals("asthinasthi")));
        assertTrue(admins.stream().anyMatch(ghUser -> ghUser.getLogin().equals("bitwiseman")));
        assertTrue(admins.stream().anyMatch(ghUser -> ghUser.getLogin().equals("farmdawgnation")));
        assertTrue(admins.stream().anyMatch(ghUser -> ghUser.getLogin().equals("halkeye")));
        assertTrue(admins.stream().anyMatch(ghUser -> ghUser.getLogin().equals("jberglund-BSFT")));
        assertTrue(admins.stream().anyMatch(ghUser -> ghUser.getLogin().equals("kohsuke")));
        assertTrue(admins.stream().anyMatch(ghUser -> ghUser.getLogin().equals("kohsuke2")));
        assertTrue(admins.stream().anyMatch(ghUser -> ghUser.getLogin().equals("martinvanzijl")));
        assertTrue(admins.stream().anyMatch(ghUser -> ghUser.getLogin().equals("PauloMigAlmeida")));
        assertTrue(admins.stream().anyMatch(ghUser -> ghUser.getLogin().equals("Sage-Pierce")));
        assertTrue(admins.stream().anyMatch(ghUser -> ghUser.getLogin().equals("timja")));
    }

    @Test
    public void testListMembersWithRole() throws IOException {
        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);

        List<GHUser> admins = org.listMembersWithRole("admin").asList();

        assertNotNull(admins);
        assertTrue(admins.size() >= 12); // In case more are added in the future
        assertTrue(admins.stream().anyMatch(ghUser -> ghUser.getLogin().equals("alexanderrtaylor")));
        assertTrue(admins.stream().anyMatch(ghUser -> ghUser.getLogin().equals("asthinasthi")));
        assertTrue(admins.stream().anyMatch(ghUser -> ghUser.getLogin().equals("bitwiseman")));
        assertTrue(admins.stream().anyMatch(ghUser -> ghUser.getLogin().equals("farmdawgnation")));
        assertTrue(admins.stream().anyMatch(ghUser -> ghUser.getLogin().equals("halkeye")));
        assertTrue(admins.stream().anyMatch(ghUser -> ghUser.getLogin().equals("jberglund-BSFT")));
        assertTrue(admins.stream().anyMatch(ghUser -> ghUser.getLogin().equals("kohsuke")));
        assertTrue(admins.stream().anyMatch(ghUser -> ghUser.getLogin().equals("kohsuke2")));
        assertTrue(admins.stream().anyMatch(ghUser -> ghUser.getLogin().equals("martinvanzijl")));
        assertTrue(admins.stream().anyMatch(ghUser -> ghUser.getLogin().equals("PauloMigAlmeida")));
        assertTrue(admins.stream().anyMatch(ghUser -> ghUser.getLogin().equals("Sage-Pierce")));
        assertTrue(admins.stream().anyMatch(ghUser -> ghUser.getLogin().equals("timja")));
    }

    @Test
    public void testCreateTeamWithRepoAccess() throws IOException {
        String REPO_NAME = "github-api";

        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHRepository repo = org.getRepository(REPO_NAME);

        // Create team with access to repository. Check access was granted.
        GHTeam team = org.createTeam(TEAM_NAME_CREATE, GHOrganization.Permission.PUSH, repo);
        Assert.assertTrue(team.getRepositories().containsKey(REPO_NAME));
        assertEquals(Permission.PUSH.toString().toLowerCase(), team.getPermission());
    }

    @Test
    public void testCreateTeam() throws IOException {
        String REPO_NAME = "github-api";
        String DEFAULT_PERMISSION = Permission.PULL.toString().toLowerCase();

        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHRepository repo = org.getRepository(REPO_NAME);

        // Create team with no permission field. Verify that default permission is pull
        GHTeam team = org.createTeam(TEAM_NAME_CREATE, repo);
        Assert.assertTrue(team.getRepositories().containsKey(REPO_NAME));
        assertEquals(DEFAULT_PERMISSION, team.getPermission());
    }

    @Test
    public void testCreateVisibleTeam() throws IOException {
        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);

        GHTeam team = org.createTeam(TEAM_NAME_CREATE).privacy(GHTeam.Privacy.CLOSED).create();
        assertEquals(GHTeam.Privacy.CLOSED, team.getPrivacy());
    }

    @Test
    public void testCreateAllArgsTeam() throws IOException {
        String REPO_NAME = "github-api";
        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);

        GHTeam team = org.createTeam(TEAM_NAME_CREATE)
                .description("Team description")
                .maintainers("bitwiseman")
                .repositories(REPO_NAME)
                .privacy(GHTeam.Privacy.CLOSED)
                .parentTeamId(3617900)
                .create();
        assertEquals("Team description", team.getDescription());
        assertEquals(GHTeam.Privacy.CLOSED, team.getPrivacy());
    }
}
