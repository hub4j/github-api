package org.kohsuke.github;

import org.junit.Test;
import org.kohsuke.github.GHTeam.Privacy;
import org.kohsuke.github.GHTeam.Role;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

// TODO: Auto-generated Javadoc
/**
 * The Class GHTeamTest.
 */
public class GHTeamTest extends AbstractGitHubWireMockTest {

    /**
     * Test set description.
     *
     * @throws IOException Signals that an I/O exception has occurred.
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
     * @return the members
     * @throws IOException Signals that an I/O exception has occurred.
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
     * @throws IOException Signals that an I/O exception has occurred.
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
     * @throws IOException Signals that an I/O exception has occurred.
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
     * @throws IOException Signals that an I/O exception has occurred.
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
     * @throws IOException Signals that an I/O exception has occurred.
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
     * @throws IOException Signals that an I/O exception has occurred.
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
     * @throws IOException Signals that an I/O exception has occurred.
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
     * @throws IOException Signals that an I/O exception has occurred.
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
}
