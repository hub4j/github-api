package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;

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

        assertThat(childTeam.getDescription(), equalTo(description));
        assertThat(childTeam.getName(), equalTo(childTeamSlug));
        assertThat(childTeam.getSlug(), equalTo(childTeamSlug));
        assertThat(childTeam.getPrivacy(), equalTo(GHTeam.Privacy.CLOSED));

    }
}
