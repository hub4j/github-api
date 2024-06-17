package org.kohsuke.github;

import org.junit.Test;
import org.kohsuke.github.GHTeam.Privacy;
import org.kohsuke.github.GHTeam.Role;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThrows;
import static org.kohsuke.github.ExternalGroupsTestingSupport.*;
import static org.kohsuke.github.ExternalGroupsTestingSupport.Matchers.isExternalGroupSummary;

// TODO: Auto-generated Javadoc
/**
 * The Class GHTeamTest.
 */
public class GHTeamTest extends AbstractGitHubWireMockTest {

    /**
     * Test set description.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testSetDescription() throws IOException {

        String description = "Updated by API Test";
        String teamSlug = "dummy-team";

        // Set the description.
        GHTeam team = gitHub.getOrganization(GITHUB_API_TEST_ORG).getTeamBySlug(teamSlug);
        assertThat(team.getHtmlUrl(), notNullValue());
        team.setDescription(description);

        // Check that it was set correctly.
        team = gitHub.getOrganization(GITHUB_API_TEST_ORG).getTeamBySlug(teamSlug);
        assertThat(team.getDescription(), equalTo(description));

        description += "Modified";

        // Set the description.
        team.setDescription(description);

        // Check that it was set correctly.
        team = gitHub.getOrganization(GITHUB_API_TEST_ORG).getTeamBySlug(teamSlug);
        assertThat(team.getDescription(), equalTo(description));
    }

    /**
     * Gets the members.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void getMembers() throws IOException {
        String teamSlug = "dummy-team";

        GHTeam team = gitHub.getOrganization(GITHUB_API_TEST_ORG).getTeamBySlug(teamSlug);

        Set<GHUser> admins = team.getMembers();

        assertThat(admins, notNullValue());
        assertThat("One admin in dummy team", admins.size(), equalTo(1));
        assertThat("Specific user in admin team",
                admins.stream().anyMatch(ghUser -> ghUser.getLogin().equals("bitwiseman")));
    }

    /**
     * List members.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void listMembers() throws IOException {
        String teamSlug = "dummy-team";

        GHTeam team = gitHub.getOrganization(GITHUB_API_TEST_ORG).getTeamBySlug(teamSlug);

        List<GHUser> admins = team.listMembers().toList();

        assertThat(admins, notNullValue());
        assertThat("One admin in dummy team", admins.size(), equalTo(1));
        assertThat("Specific user in admin team",
                admins.stream().anyMatch(ghUser -> ghUser.getLogin().equals("bitwiseman")));
    }

    /**
     * List members admin.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void listMembersAdmin() throws IOException {
        String teamSlug = "dummy-team";

        GHTeam team = gitHub.getOrganization(GITHUB_API_TEST_ORG).getTeamBySlug(teamSlug);

        List<GHUser> admins = team.listMembers("admin").toList();

        assertThat(admins, notNullValue());
        assertThat("One admin in dummy team", admins.size(), equalTo(1));
        assertThat("Specific user in admin team",
                admins.stream().anyMatch(ghUser -> ghUser.getLogin().equals("bitwiseman")));
    }

    /**
     * List members no match.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void listMembersNoMatch() throws IOException {
        String teamSlug = "dummy-team";

        GHTeam team = gitHub.getOrganization(GITHUB_API_TEST_ORG).getTeamBySlug(teamSlug);

        List<GHUser> justMembers = team.listMembers("member").toList();

        assertThat("No regular members in team", justMembers.isEmpty());
    }

    /**
     * Test set privacy.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testSetPrivacy() throws IOException {
        // we need to use a team that doesn't have child teams
        // as secret privacy is not supported for parent teams
        String teamSlug = "simple-team";
        Privacy privacy = Privacy.CLOSED;

        // Set the privacy.
        GHTeam team = gitHub.getOrganization(GITHUB_API_TEST_ORG).getTeamBySlug(teamSlug);
        team.setPrivacy(privacy);

        // Check that it was set correctly.
        team = gitHub.getOrganization(GITHUB_API_TEST_ORG).getTeamBySlug(teamSlug);
        assertThat(team.getPrivacy(), equalTo(privacy));

        privacy = Privacy.SECRET;

        // Set the privacy.
        team.setPrivacy(privacy);

        // Check that it was set correctly.
        team = gitHub.getOrganization(GITHUB_API_TEST_ORG).getTeamBySlug(teamSlug);
        assertThat(team.getPrivacy(), equalTo(privacy));
    }

    /**
     * Test fetch child teams.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testFetchChildTeams() throws IOException {
        String teamSlug = "dummy-team";

        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHTeam team = org.getTeamBySlug(teamSlug);
        Set<GHTeam> result = team.listChildTeams().toSet();

        assertThat(result.size(), equalTo(1));
        assertThat(result.toArray(new GHTeam[]{})[0].getName(), equalTo("child-team-for-dummy"));
    }

    /**
     * Test fetch empty child teams.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testFetchEmptyChildTeams() throws IOException {
        String teamSlug = "simple-team";

        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHTeam team = org.getTeamBySlug(teamSlug);
        Set<GHTeam> result = team.listChildTeams().toSet();

        assertThat(result, is(empty()));
    }

    /**
     * Adds the remove member.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void addRemoveMember() throws IOException {
        String teamSlug = "dummy-team";

        GHTeam team = gitHub.getOrganization(GITHUB_API_TEST_ORG).getTeamBySlug(teamSlug);

        List<GHUser> members = team.listMembers().toList();

        assertThat(members, notNullValue());
        assertThat("One admin in dummy team", members.size(), equalTo(1));
        assertThat("Specific user in admin team",
                members.stream().anyMatch(ghUser -> ghUser.getLogin().equals("bitwiseman")));

        GHUser user = gitHub.getUser("gsmet");

        try {
            team.add(user, Role.MAINTAINER);

            // test all
            members = team.listMembers().toList();

            assertThat(members, notNullValue());
            assertThat("Two members for all roles in dummy team", members.size(), equalTo(2));
            assertThat("Specific users in team",
                    members,
                    containsInAnyOrder(hasProperty("login", equalTo("bitwiseman")),
                            hasProperty("login", equalTo("gsmet"))));

            // test maintainer role filter
            members = team.listMembers(Role.MAINTAINER).toList();

            assertThat(members, notNullValue());
            assertThat("Two members for all roles in dummy team", members.size(), equalTo(2));
            assertThat("Specific users in team",
                    members,
                    containsInAnyOrder(hasProperty("login", equalTo("bitwiseman")),
                            hasProperty("login", equalTo("gsmet"))));

            // test member role filter
            // it's hard to test this as owner of the org are automatically made maintainer
            // so let's just test that we don't have any members around
            members = team.listMembers(Role.MEMBER).toList();

            assertThat(members, notNullValue());
            assertThat("No members in dummy team", members.size(), equalTo(0));

            // test removing the user has effect
            team.remove(user);

            members = team.listMembers().toList();

            assertThat(members, notNullValue());
            assertThat("One member for all roles in dummy team", members.size(), equalTo(1));
            assertThat("Specific user in team",
                    members,
                    containsInAnyOrder(hasProperty("login", equalTo("bitwiseman"))));
        } finally {
            if (team.hasMember(user)) {
                team.remove(user);
            }
        }
    }

    /**
     * Test get external groups.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testGetExternalGroups() throws IOException {
        String teamSlug = "acme-developers";

        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHTeam team = org.getTeamBySlug(teamSlug);
        final List<GHExternalGroup> groups = team.getExternalGroups();

        assertThat(groups, notNullValue());
        assertThat(groups.size(), equalTo(1));
        assertThat(groupSummary(groups), hasItems("467431:acme-developers"));

        groups.forEach(group -> assertThat(group, isExternalGroupSummary()));
    }

    /**
     * Test get external groups from not enterprise managed organization.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testGetExternalGroupsNotEnterpriseManagedOrganization() throws IOException {
        String teamSlug = "acme-developers";

        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHTeam team = org.getTeamBySlug(teamSlug);

        final GHIOException failure = assertThrows(GHNotExternallyManagedEnterpriseException.class,
                () -> team.getExternalGroups());
        assertThat(failure.getMessage(), equalTo("Could not retrieve team external groups"));
    }

    /**
     * Test get external groups from team that cannot be externally managed.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testGetExternalGroupsTeamCannotBeExternallyManaged() throws IOException {
        String teamSlug = "acme-developers";

        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHTeam team = org.getTeamBySlug(teamSlug);

        final GHIOException failure = assertThrows(GHTeamCannotBeExternallyManagedException.class,
                () -> team.getExternalGroups());
        assertThat(failure.getMessage(), equalTo("Could not retrieve team external groups"));
    }

    /**
     * Test connect to external group by id.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testConnectToExternalGroupById() throws IOException {
        String teamSlug = "acme-developers";

        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHTeam team = org.getTeamBySlug(teamSlug);

        final GHExternalGroup group = team.connectToExternalGroup(467431);

        assertThat(group.getId(), equalTo(467431L));
        assertThat(group.getName(), equalTo("acme-developers"));
        assertThat(group.getUpdatedAt(), notNullValue());

        assertThat(group.getMembers(), notNullValue());
        assertThat(membersSummary(group),
                hasItems("158311279:john-doe_acme:John Doe:john.doe@acme.corp",
                        "166731041:jane-doe_acme:Jane Doe:jane.doe@acme.corp"));

        assertThat(group.getTeams(), notNullValue());
        assertThat(teamSummary(group), hasItems("34519919:ACME-DEVELOPERS"));
    }

    /**
     * Test fail to connect to external group from other organization.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testConnectToExternalGroupByGroup() throws IOException {
        String teamSlug = "acme-developers";

        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHTeam team = org.getTeamBySlug(teamSlug);
        GHExternalGroup group = org.getExternalGroup(467431);

        GHExternalGroup connectedGroup = team.connectToExternalGroup(group);

        assertThat(connectedGroup.getId(), equalTo(467431L));
        assertThat(connectedGroup.getName(), equalTo("acme-developers"));
        assertThat(connectedGroup.getUpdatedAt(), notNullValue());

        assertThat(connectedGroup.getMembers(), notNullValue());
        assertThat(membersSummary(connectedGroup),
                hasItems("158311279:john-doe_acme:John Doe:john.doe@acme.corp",
                        "166731041:jane-doe_acme:Jane Doe:jane.doe@acme.corp"));

        assertThat(group.getTeams(), notNullValue());
        assertThat(teamSummary(connectedGroup), hasItems("34519919:ACME-DEVELOPERS"));
    }

    /**
     * Test failure when connecting to external group by id.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testFailConnectToExternalGroupWhenTeamHasMembers() throws IOException {
        String teamSlug = "acme-developers";

        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHTeam team = org.getTeamBySlug(teamSlug);

        final GHIOException failure = assertThrows(GHTeamCannotBeExternallyManagedException.class,
                () -> team.connectToExternalGroup(467431));
        assertThat(failure.getMessage(), equalTo("Could not connect team to external group"));
    }

    /**
     * Test failure when connecting to external group by id.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testFailConnectToExternalGroupTeamIsNotAvailableInOrg() throws IOException {
        String teamSlug = "acme-developers";

        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHTeam team = org.getTeamBySlug(teamSlug);

        assertThrows(GHFileNotFoundException.class, () -> team.connectToExternalGroup(12345));
    }

    /**
     * Test delete connection to external group
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testDeleteExternalGroupConnection() throws IOException {
        String teamSlug = "acme-developers";

        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHTeam team = org.getTeamBySlug(teamSlug);

        team.deleteExternalGroupConnection();

        mockGitHub.apiServer()
                .verify(1,
                        deleteRequestedFor(urlPathEqualTo("/orgs/" + team.getOrganization().getLogin() + "/teams/"
                                + team.getSlug() + "/external-groups")));
    }

}
