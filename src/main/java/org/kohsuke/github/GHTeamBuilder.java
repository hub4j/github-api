package org.kohsuke.github;

import java.io.IOException;

/**
 * Creates a team.
 *
 * https://developer.github.com/v3/teams/#create-team
 */
public class GHTeamBuilder {

    private final GitHub root;
    protected final Requester builder;
    private final String orgName;

    public GHTeamBuilder(GitHub root, String orgName, String name) {
        this.root = root;
        this.orgName = orgName;
        this.builder = root.createRequest();
        this.builder.with("name", name);
    }

    /**
     * Description for this team.
     *
     * @param description
     *            description of team
     * @return a builder to continue with building
     */
    public GHTeamBuilder description(String description) {
        this.builder.with("description", description);
        return this;
    }

    /**
     * Maintainers for this team.
     *
     * @param maintainers
     *            maintainers of team
     * @return a builder to continue with building
     */
    public GHTeamBuilder maintainers(String... maintainers) {
        this.builder.with("maintainers", maintainers);
        return this;
    }

    /**
     * Repository names to add this team to.
     *
     * @param repoNames
     *            repoNames to add team to
     * @return a builder to continue with building
     */
    public GHTeamBuilder repositories(String... repoNames) {
        this.builder.with("repo_names", repoNames);
        return this;
    }

    /**
     * Description for this team
     *
     * @param privacy
     *            privacy of team
     * @return a builder to continue with building
     */
    public GHTeamBuilder privacy(GHTeam.Privacy privacy) {
        this.builder.with("privacy", privacy);
        return this;
    }

    /**
     * Parent team id for this team
     *
     * @param parentTeamId
     *            parentTeamId of team
     * @return a builder to continue with building
     */
    public GHTeamBuilder parentTeamId(int parentTeamId) {
        this.builder.with("parent_team_id", parentTeamId);
        return this;
    }

    /**
     * Creates a team with all the parameters.
     *
     * @return the gh team
     * @throws IOException
     *             if team cannot be created
     */
    public GHTeam create() throws IOException {
        return builder.method("POST").withUrlPath("/orgs/" + orgName + "/teams").fetch(GHTeam.class).wrapUp(root);
    }
}
