package org.kohsuke.github;

import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;

// TODO: Auto-generated Javadoc

/**
 * The Class GHTeamUpdateBuilderTest.
 *
 * @author Rory Kelly
 */
public class GHTeamUpdateBuilderTest extends AbstractGitHubWireMockTest {

    private static final String TEAM_TO_UPDATE_SLUG = "dummy-team-to-update";

    private static final String TEAM_TO_UPDATE_NEW_NAME = "dummy-team-updated";
    private static final String TEAM_TO_UPDATE_NEW_DESCRIPTION = "This is an updated description!";
    private static final GHTeam.Privacy TEAM_TO_UPDATE_NEW_PRIVACY = GHTeam.Privacy.SECRET;
    private static final GHTeam.NotificationSetting TEAM_TO_UPDATE_NEW_NOTIFICATIONS = GHTeam.NotificationSetting.NOTIFICATIONS_DISABLED;
    @Deprecated
    private static final GHOrganization.Permission TEAM_TO_UPDATE_NEW_PERMISSIONS = GHOrganization.Permission.PUSH;

    // private static final String CURRENT_PARENT_TEAM_SLUG = "dummy-current-parent-team";
    private static final String NEW_PARENT_TEAM_SLUG = "dummy-new-parent-team";

    /**
     * Create default GHTeamBuilderTest instance
     */
    public GHTeamUpdateBuilderTest() {
    }

    /**
     * Given a team, when updating the team with a different parent team, then the team is updated with the new parent team.
     * @throws IOException exception thrown if there is an issue with Wiremock
     */
    @Test
    public void testUpdateTeamWithNewParentTeam() throws IOException {
        // Get the org and teams
        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHTeam teamToUpdate = org.getTeamBySlug(TEAM_TO_UPDATE_SLUG);
        GHTeam newParentTeam = org.getTeamBySlug(NEW_PARENT_TEAM_SLUG);

        // Update team with different parent team
        GHTeam updatedTeam = getCommonBuilder(teamToUpdate)
                .parentTeamId(newParentTeam.getId())
                .update();

        assertUpdatedTeam(updatedTeam);
        // assertThat(updatedTeam.getParentTeam().getId(), equalTo(newParentTeam.getId()));
    }

    /**
     * Given a team, when updating the team with no change to parent team, then the team is updated with no change to the parent team.
     * @throws IOException exception thrown if there is an issue with Wiremock
     */
    @Test
    public void testUpdateTeamWithNoChangeToParentTeam() throws IOException {
        // Get the org and team
        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHTeam teamToUpdate = org.getTeamBySlug(TEAM_TO_UPDATE_SLUG);
        // GHTeam existingParentTeam = org.getTeamBySlug(CURRENT_PARENT_TEAM_SLUG);

        // update team with no change to parent team
        GHTeam updatedTeam = getCommonBuilder(teamToUpdate).update();

        assertUpdatedTeam(updatedTeam);
        // assertThat(teamToUpdate.getParentTeam().getId(), equalTo(existingParentTeam.getId()));
    }

    /**
     * Given a team, when updating the team with a removed parent team, then the team is updated and has no parent team.
     * @throws IOException exception thrown if there is an issue with Wiremock
     */
    @Test
    public void testUpdateTeamWithRemovedParentTeam() throws IOException {
        // Get the org and team
        GHOrganization org = gitHub.getOrganization(GITHUB_API_TEST_ORG);
        GHTeam teamToUpdate = org.getTeamBySlug(TEAM_TO_UPDATE_SLUG);

        // Update team with removed parent team
        GHTeam updatedTeam = getCommonBuilder(teamToUpdate)
                .parentTeamId(null)
                .update();

        assertUpdatedTeam(updatedTeam);
        // assertThat(teamToUpdate.getParentTeam(), equalTo(null));
    }

    /**
     * Get the GHTeamUpdateBuilder instance with the common fields set for updating a team, to be used in the different update scenarios.
     *
     * @param teamToUpdate the base team to update
     * @return the GHTeamUpdateBuilder instance with the common fields set for updating a team
     */
    private GHTeamUpdateBuilder getCommonBuilder(GHTeam teamToUpdate) {
        return teamToUpdate.updateTeam()
                .name(TEAM_TO_UPDATE_NEW_NAME)
                .description(TEAM_TO_UPDATE_NEW_DESCRIPTION)
                .privacy(TEAM_TO_UPDATE_NEW_PRIVACY)
                .notifications(TEAM_TO_UPDATE_NEW_NOTIFICATIONS)
                .permission(TEAM_TO_UPDATE_NEW_PERMISSIONS);
    }

    /**
     * Assert that the updated team has the expected updated values.
     *
     * @param updatedTeam the team to assert the updated values on
     */
    private void assertUpdatedTeam(GHTeam updatedTeam) {
        assertThat(updatedTeam.getName(), equalTo(TEAM_TO_UPDATE_NEW_NAME));
        assertThat(updatedTeam.getDescription(), equalTo(TEAM_TO_UPDATE_NEW_DESCRIPTION));
        assertThat(updatedTeam.getPrivacy(), equalTo(TEAM_TO_UPDATE_NEW_PRIVACY));
        // assertThat(updatedTeam.getNotificationSetting(), equalTo(TEAM_TO_UPDATE_NEW_NOTIFICATIONS));
        assertThat(updatedTeam.getPermission(), equalTo(TEAM_TO_UPDATE_NEW_PERMISSIONS));
    }
}
