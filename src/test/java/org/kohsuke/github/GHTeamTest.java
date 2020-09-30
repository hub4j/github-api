package org.kohsuke.github;

import org.junit.Test;
import org.kohsuke.github.GHTeam.Privacy;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class GHTeamTest extends AbstractGitHubWireMockTest {

    @Test
    public void testSetDescription() throws IOException {

        String description = "Updated by API Test";
        String teamSlug = "dummy-team";

        // Set the description.
        GHTeam team = gitHub.getOrganization(GITHUB_API_TEST_ORG).getTeamBySlug(teamSlug);
        team.setDescription(description);

        // Check that it was set correctly.
        team = gitHub.getOrganization(GITHUB_API_TEST_ORG).getTeamBySlug(teamSlug);
        assertEquals(description, team.getDescription());

        description += "Modified";

        // Set the description.
        team.setDescription(description);

        // Check that it was set correctly.
        team = gitHub.getOrganization(GITHUB_API_TEST_ORG).getTeamBySlug(teamSlug);
        assertEquals(description, team.getDescription());
    }

    @Test
    public void testlistMembersAdmin() throws IOException {
        String teamSlug = "dummy-team";

        GHTeam team = gitHub.getOrganization(GITHUB_API_TEST_ORG).getTeamBySlug(teamSlug);

        List<GHUser> admins = team.listMembers("admin").asList();

        assertNotNull(admins);
        assertThat("One admin in dummy team", admins.size() == 1);
        assertThat("Specific user in admin team",
                admins.stream().anyMatch(ghUser -> ghUser.getLogin().equals("bitwiseman")));
    }

    @Test
    public void testlistMembersNoMatch() throws IOException {
        String teamSlug = "dummy-team";

        GHTeam team = gitHub.getOrganization(GITHUB_API_TEST_ORG).getTeamBySlug(teamSlug);

        List<GHUser> justMembers = team.listMembers("member").asList();

        assertThat("No regular members in team", justMembers.isEmpty());
    }

    @Test
    public void testSetPrivacy() throws IOException {
        String teamSlug = "dummy-team";
        Privacy privacy = Privacy.CLOSED;

        // Set the privacy.
        GHTeam team = gitHub.getOrganization(GITHUB_API_TEST_ORG).getTeamBySlug(teamSlug);
        team.setPrivacy(privacy);

        // Check that it was set correctly.
        team = gitHub.getOrganization(GITHUB_API_TEST_ORG).getTeamBySlug(teamSlug);
        assertEquals(privacy, team.getPrivacy());

        privacy = Privacy.SECRET;

        // Set the privacy.
        team.setPrivacy(privacy);

        // Check that it was set correctly.
        team = gitHub.getOrganization(GITHUB_API_TEST_ORG).getTeamBySlug(teamSlug);
        assertEquals(privacy, team.getPrivacy());
    }

    @Test
    public void testFetchChildTeams() throws IOException {
        String teamSlug = "dummy-team";

        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHTeam team = org.getTeamBySlug(teamSlug);
        Set<GHTeam> result = team.listChildTeams().toSet();

        assertEquals(1, result.size());
        assertEquals("child-team-for-dummy", result.toArray(new GHTeam[]{})[0].getName());
    }

    @Test
    public void testFetchEmptyChildTeams() throws IOException {
        String teamSlug = "simple-team";

        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHTeam team = org.getTeamBySlug(teamSlug);
        Set<GHTeam> result = team.listChildTeams().toSet();

        assertEquals(0, result.size());
    }

}
