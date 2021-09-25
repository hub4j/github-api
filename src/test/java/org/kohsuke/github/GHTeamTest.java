package org.kohsuke.github;

import org.junit.Test;
import org.kohsuke.github.GHTeam.Privacy;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;

public class GHTeamTest extends AbstractGitHubWireMockTest {

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

    @Test
    public void listMembersNoMatch() throws IOException {
        String teamSlug = "dummy-team";

        GHTeam team = gitHub.getOrganization(GITHUB_API_TEST_ORG).getTeamBySlug(teamSlug);

        List<GHUser> justMembers = team.listMembers("member").toList();

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
        assertThat(team.getPrivacy(), equalTo(privacy));

        privacy = Privacy.SECRET;

        // Set the privacy.
        team.setPrivacy(privacy);

        // Check that it was set correctly.
        team = gitHub.getOrganization(GITHUB_API_TEST_ORG).getTeamBySlug(teamSlug);
        assertThat(team.getPrivacy(), equalTo(privacy));
    }

    @Test
    public void testFetchChildTeams() throws IOException {
        String teamSlug = "dummy-team";

        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHTeam team = org.getTeamBySlug(teamSlug);
        Set<GHTeam> result = team.listChildTeams().toSet();

        assertThat(result.size(), equalTo(1));
        assertThat(result.toArray(new GHTeam[]{})[0].getName(), equalTo("child-team-for-dummy"));
    }

    @Test
    public void testFetchEmptyChildTeams() throws IOException {
        String teamSlug = "simple-team";

        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHTeam team = org.getTeamBySlug(teamSlug);
        Set<GHTeam> result = team.listChildTeams().toSet();

        assertThat(result, is(empty()));
    }

}
