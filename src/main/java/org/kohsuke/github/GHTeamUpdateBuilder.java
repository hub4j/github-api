package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;

/**
 * Updates a team.
 *
 * @see <a href=https://developer.github.com/v3/teams/#update-team>Update team docs</a>
 * @author Rory Kelly
 */
public class GHTeamUpdateBuilder extends GitHubInteractiveObject {

    private final String orgName;
    private final String existingName;
    /** The builder. */
    protected final Requester builder;

    /**
     * Instantiates a new GH team update builder.
     *
     * @param root
     *            the root
     * @param orgName
     *            the org name
     * @param existingName
     *            the current name of the existing team
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
    public GHTeamUpdateBuilder(GitHub root, String orgName, String existingName) {
        super(root);
        this.orgName = orgName;
        this.existingName = existingName;
        this.builder = root.createRequest();
    }

    /**
     * Updates a team with all the provided parameters.
     *
     * @return the gh team
     * @throws IOException
     *             if team cannot be updated
     */
    public GHTeam update() throws IOException {
        return builder.method("PATCH").withUrlPath("/orgs/" + orgName + "/teams/" + existingName).fetch(GHTeam.class).wrapUp(root());
    }

    /**
     * Name for this team.
     *
     * @param name
     *            name of team
     * @return a builder to continue with building
     */
    public GHTeamUpdateBuilder name(String name) {
        this.builder.with("name", name);
        return this;
    }

    /**
     * Description for this team.
     *
     * @param description
     *            description of team
     * @return a builder to continue with building
     */
    public GHTeamUpdateBuilder description(String description) {
        this.builder.with("description", description);
        return this;
    }

    /**
     * Privacy for this team.
     *
     * @param privacy
     *            privacy of team
     * @return a builder to continue with building
     */
    public GHTeamUpdateBuilder privacy(GHTeam.Privacy privacy) {
        this.builder.with("privacy", privacy);
        return this;
    }

    /**
     * The notification setting explicitly set for this team.
     *
     * @param notificationSetting
     *            notification setting to be applied
     * @return a builder to continue with building
     */
    public GHTeamUpdateBuilder notifications(GHTeam.NotificationSetting notificationSetting) {
        this.builder.with("notification_setting", notificationSetting);
        return this;
    }

    /**
     * The permission that new repositories will be added to the team with when none is specified.
     *
     * @param permission
     *            permission to be applied
     * @return a builder to continue with building
     * @deprecated see
     *              <a href=https://developer.github.com/v3/teams/#update-team>Update team docs</a>
     */
    @Deprecated
    public GHTeamUpdateBuilder permission(GHOrganization.Permission permission) {
        this.builder.with("permission", permission);
        return this;
    }

    /**
     * Parent team id for this team.
     *
     * @param parentTeamId
     *            parentTeamId of team, or null if you are removing the parent
     * @return a builder to continue with building
     */
    public GHTeamUpdateBuilder parentTeamId(Long parentTeamId) {
        this.builder.with("parent_team_id", parentTeamId);
        return this;
    }
}
