package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;

// TODO: Auto-generated Javadoc
/**
 * Creates a team.
 *
 * https://developer.github.com/v3/teams/#create-team
 */
public class GHTeamBuilder extends GitHubInteractiveObject {

    /** The builder. */
    protected final Requester builder;
    private final String orgName;

    /**
     * Instantiates a new GH team builder.
     *
     * @param root
     *            the root
     * @param orgName
     *            the org name
     * @param name
     *            the name
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected")
    public GHTeamBuilder(GitHub root, String orgName, String name) {
        super(root);
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
     * Description for this team.
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
     * Parent team id for this team.
     *
     * @param parentTeamId
     *            parentTeamId of team
     * @return a builder to continue with building
     */
    public GHTeamBuilder parentTeamId(long parentTeamId) {
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
        return builder.method("POST").withUrlPath("/orgs/" + orgName + "/teams").fetch(GHTeam.class).wrapUp(root());
    }
}
