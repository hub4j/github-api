package org.kohsuke.github;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.github.GHOrganization.Permission;
import org.kohsuke.github.GHOrganization.RepositoryRole;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;

// TODO: Auto-generated Javadoc
/**
 * The Class GHOrganizationTest.
 */
public class GHOrganizationTest extends AbstractGitHubWireMockTest {

    /** The Constant GITHUB_API_TEST. */
    public static final String GITHUB_API_TEST = "github-api-test";

    /** The Constant GITHUB_API_TEMPLATE_TEST. */
    public static final String GITHUB_API_TEMPLATE_TEST = "github-api-template-test";

    /** The Constant TEAM_NAME_CREATE. */
    public static final String TEAM_NAME_CREATE = "create-team-test";

    /**
     * Clean up team.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
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

        getNonRecordingGitHub().getOrganization(GITHUB_API_TEST_ORG).enableOrganizationProjects(true);
    }

    /**
     * Test create repository.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
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

    /**
     * Test create repository with auto initialization.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
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

    /**
     * Test create repository with parameter is template.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
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

    /**
     * Test create repository with template.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
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

    /**
     * Test invite user.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
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

    /**
     * Test list members with filter.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
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

    /**
     * Test list members with role.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
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
    
    /**
     * Test list outside collaborators with filter.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testListOutsideCollaboratorsWithFilter() throws IOException {
        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);

        List<GHUser> admins = org.listOutsideCollaboratorsWithFilter("all").toList();

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

    /**
     * Test create team with repo access.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
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

    /**
     * Test create team with null perm.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testCreateTeamWithNullPerm() throws Exception {
        String REPO_NAME = "github-api";

        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHRepository repo = org.getRepository(REPO_NAME);

        // Create team with access to repository. Check access was granted.
        GHTeam team = org.createTeam(TEAM_NAME_CREATE).create();

        team.add(repo);

        assertThat(
                repo.getTeams()
                        .stream()
                        .filter(t -> TEAM_NAME_CREATE.equals(t.getName()))
                        .findFirst()
                        .get()
                        .getPermission(),
                equalTo(Permission.PULL.toString().toLowerCase()));
    }

    /**
     * Test create team with repo perm.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testCreateTeamWithRepoPerm() throws Exception {
        String REPO_NAME = "github-api";

        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHRepository repo = org.getRepository(REPO_NAME);

        // Create team with access to repository. Check access was granted.
        GHTeam team = org.createTeam(TEAM_NAME_CREATE).create();

        team.add(repo, GHOrganization.Permission.PUSH);

        assertThat(
                repo.getTeams()
                        .stream()
                        .filter(t -> TEAM_NAME_CREATE.equals(t.getName()))
                        .findFirst()
                        .get()
                        .getPermission(),
                equalTo(Permission.PUSH.toString().toLowerCase()));

    }

    /**
     * Test create team with repo role.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testCreateTeamWithRepoRole() throws IOException {
        String REPO_NAME = "github-api";

        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHRepository repo = org.getRepository(REPO_NAME);

        // Create team with access to repository. Check access was granted.
        GHTeam team = org.createTeam(TEAM_NAME_CREATE).create();

        RepositoryRole role = RepositoryRole.from(GHOrganization.Permission.TRIAGE);
        team.add(repo, role);

        // 'getPermission' does not return triage even though the UI shows that value
        // assertThat(
        // repo.getTeams()
        // .stream()
        // .filter(t -> TEAM_NAME_CREATE.equals(t.getName()))
        // .findFirst()
        // .get()
        // .getPermission(),
        // equalTo(role.toString()));
    }

    /**
     * Test create team.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
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

    /**
     * Test create visible team.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testCreateVisibleTeam() throws IOException {
        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);

        GHTeam team = org.createTeam(TEAM_NAME_CREATE).privacy(GHTeam.Privacy.CLOSED).create();
        assertThat(team.getPrivacy(), equalTo(GHTeam.Privacy.CLOSED));
    }

    /**
     * Test create all args team.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
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

    /**
     * Test are organization projects enabled.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testAreOrganizationProjectsEnabled() throws IOException {
        // Arrange
        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);

        // Act
        boolean result = org.areOrganizationProjectsEnabled();

        // Assert
        assertThat(result, is(true));
    }

    /**
     * Test enable organization projects.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testEnableOrganizationProjects() throws IOException {
        // Arrange
        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);

        // Act
        org.enableOrganizationProjects(false);

        // Assert
        assertThat(org.areOrganizationProjectsEnabled(), is(false));
    }
}
