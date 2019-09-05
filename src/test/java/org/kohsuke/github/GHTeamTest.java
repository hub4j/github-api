package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;
import java.util.Random;

public class GHTeamTest extends AbstractGitHubApiTestBase {

    @Test
    public void testSetDescription() throws IOException {

        Random random = new Random();
        String description = "Updated by API Test " + String.valueOf(random.nextInt());
        String organizationSlug = "martinvz-test";
        String teamSlug = "dummy-team";

        // Set the description.
        {
            GHTeam team = gitHub.getOrganization(organizationSlug).getTeamBySlug(teamSlug);
            team.setDescription(description);
        }

        // Check that it was set correctly.
        {
            GHTeam team = gitHub.getOrganization(organizationSlug).getTeamBySlug(teamSlug);
            assertEquals(description, team.getDescription());
        }
    }
}
