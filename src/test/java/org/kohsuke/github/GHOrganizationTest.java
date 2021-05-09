package org.kohsuke.github;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.github.GHOrganization.Permission;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;

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

        GHTeam team = getNonRecordingGitHub().getOrganization(GITHUB_API_TEST_ORG).getTeamByName(TEAM_NAME_CREATE);
        if (team != null) {
            team.delete();
        }

        getNonRecordingGitHub().getOrganization(GITHUB_API_TEST_ORG).root.createRequest()
                .withUrlPath("/orgs/" + GITHUB_API_TEST_ORG)
                .method("PATCH")
                .with("has_organization_projects", true)
                .send();
    }

    @Test
    public void testCreateRepository() throws IOException {
        cleanupRepository(GITHUB_API_TEST_ORG + '/' + GITHUB_API_TEST);

        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHRepository repository = org.createRepository(GITHUB_API_TEST)
                .description("a test repository used to test kohsuke's github-api")
                .homepage("http://github-api.kohsuke.org/")
                .team(org.getTeamByName("Core Developers"))
                .private_(false)
                .create();
        assertThat(repository, notNullValue());
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
        assertThat(repository, notNullValue());
        assertThat(repository.getReadme(), notNullValue());
    }

    @Test
    public void testCreateRepositoryWithParameterIsTemplate() throws IOException {
        cleanupRepository(GITHUB_API_TEST_ORG + '/' + GITHUB_API_TEMPLATE_TEST);

        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHTeam team = org.getTeamByName("Core Developers");

        int requestCount = mockGitHub.getRequestCount();
        GHRepository repository = org.createRepository(GITHUB_API_TEMPLATE_TEST)
                .description("a test template repository used to test kohsuke's github-api")
                .homepage("http://github-api.kohsuke.org/")
                .team(team)
                .autoInit(true)
                .isTemplate(true)
                .create();
        assertThat(repository, notNullValue());
        assertThat(mockGitHub.getRequestCount(), equalTo(requestCount + 1));

        assertThat(repository.getReadme(), notNullValue());
        assertThat(mockGitHub.getRequestCount(), equalTo(requestCount + 2));

        // isTemplate() does not call populate() from create
        assertThat(repository.isTemplate(), equalTo(true));
        assertThat(mockGitHub.getRequestCount(), equalTo(requestCount + 2));

        repository = org.getRepository(GITHUB_API_TEMPLATE_TEST);

        // first isTemplate() calls populate()
        assertThat(repository.isTemplate(), equalTo(true));
        assertThat(mockGitHub.getRequestCount(), equalTo(requestCount + 4));

        // second isTemplate() does not call populate()
        assertThat(repository.isTemplate(), equalTo(true));
        assertThat(mockGitHub.getRequestCount(), equalTo(requestCount + 4));

    }

    @Test
    public void testCreateRepositoryWithTemplate() throws IOException {
        cleanupRepository(GITHUB_API_TEST_ORG + '/' + GITHUB_API_TEST);

        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHRepository repository = org.createRepository(GITHUB_API_TEST)
                .fromTemplateRepository(GITHUB_API_TEST_ORG, GITHUB_API_TEMPLATE_TEST)
                .owner(GITHUB_API_TEST_ORG)
                .create();

        assertThat(repository, notNullValue());
        assertThat(repository.getReadme(), notNullValue());

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

        List<GHUser> admins = org.listMembersWithFilter("all").toList();

        assertThat(admins, notNullValue());
        // In case more are added in the future
        assertThat(admins.size(), greaterThanOrEqualTo(12));
        assertThat(admins.stream().map(GHUser::getLogin).collect(Collectors.toList()),
                hasItems("alexanderrtaylor",
                        "asthinasthi",
                        "bitwiseman",
                        "farmdawgnation",
                        "halkeye",
                        "jberglund-BSFT",
                        "kohsuke",
                        "kohsuke2",
                        "martinvanzijl",
                        "PauloMigAlmeida",
                        "Sage-Pierce",
                        "timja"));
    }

    @Test
    public void testListMembersWithRole() throws IOException {
        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);

        List<GHUser> admins = org.listMembersWithRole("admin").toList();

        assertThat(admins, notNullValue());
        // In case more are added in the future
        assertThat(admins.size(), greaterThanOrEqualTo(12));
        assertThat(admins.stream().map(GHUser::getLogin).collect(Collectors.toList()),
                hasItems("alexanderrtaylor",
                        "asthinasthi",
                        "bitwiseman",
                        "farmdawgnation",
                        "halkeye",
                        "jberglund-BSFT",
                        "kohsuke",
                        "kohsuke2",
                        "martinvanzijl",
                        "PauloMigAlmeida",
                        "Sage-Pierce",
                        "timja"));
    }

    @Test
    public void testCreateTeamWithRepoAccess() throws IOException {
        String REPO_NAME = "github-api";

        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHRepository repo = org.getRepository(REPO_NAME);

        // Create team with access to repository. Check access was granted.
        GHTeam team = org.createTeam(TEAM_NAME_CREATE, GHOrganization.Permission.PUSH, repo);
        assertThat(team.getRepositories().containsKey(REPO_NAME), is(true));
        assertThat(team.getPermission(), equalTo(Permission.PUSH.toString().toLowerCase()));
    }

    @Test
    public void testCreateTeam() throws IOException {
        String REPO_NAME = "github-api";
        String DEFAULT_PERMISSION = Permission.PULL.toString().toLowerCase();

        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHRepository repo = org.getRepository(REPO_NAME);

        // Create team with no permission field. Verify that default permission is pull
        GHTeam team = org.createTeam(TEAM_NAME_CREATE, repo);
        assertThat(team.getRepositories().containsKey(REPO_NAME), is(true));
        assertThat(team.getPermission(), equalTo(DEFAULT_PERMISSION));
    }

    @Test
    public void testCreateVisibleTeam() throws IOException {
        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);

        GHTeam team = org.createTeam(TEAM_NAME_CREATE).privacy(GHTeam.Privacy.CLOSED).create();
        assertThat(team.getPrivacy(), equalTo(GHTeam.Privacy.CLOSED));
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
        assertThat(team.getDescription(), equalTo("Team description"));
        assertThat(team.getPrivacy(), equalTo(GHTeam.Privacy.CLOSED));
    }

    @Test
    public void testAreOrganizationProjectsEnabled() throws IOException {
        // Arrange
        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);

        // Act
        boolean result = org.areOrganizationProjectsEnabled();

        // Assert that projects are enabled
        assertThat(result, is(true));
    }
}
