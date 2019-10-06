package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;
import java.util.Random;

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


}
