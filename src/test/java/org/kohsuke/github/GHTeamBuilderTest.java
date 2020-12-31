package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;

public class GHTeamBuilderTest extends AbstractGitHubWireMockTest {

    @Test
    public void testCreateChildTeam() throws IOException {
        String parentTeamSlug = "dummy-team";
        String childTeamSlug = "dummy-team-child";
        String description = "description";

        // Get the parent team
        GHTeam parentTeam = gitHub.getOrganization(GITHUB_API_TEST_ORG).getTeamBySlug(parentTeamSlug);

        // Create a child team, using the parent team identifier
        GHTeam childTeam = gitHub.getOrganization(GITHUB_API_TEST_ORG)
                .createTeam(childTeamSlug)
                .description(description)
                .privacy(GHTeam.Privacy.CLOSED)
                .parentTeamId(parentTeam.getId())
                .create();

        assertEquals(description, childTeam.getDescription());
        assertEquals(childTeamSlug, childTeam.getName());
        assertEquals(childTeamSlug, childTeam.getSlug());
        assertEquals(GHTeam.Privacy.CLOSED, childTeam.getPrivacy());

    }
}
