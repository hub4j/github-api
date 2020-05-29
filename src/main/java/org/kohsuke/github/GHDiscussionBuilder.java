package org.kohsuke.github;

import java.io.IOException;

/**
 * Creates a Discussion.
 *
 * https://developer.github.com/v3/teams/discussions/#create-a-discussion
 */
public class GHDiscussionBuilder {

    protected final Requester builder;
    private final GHTeam team;

    public GHDiscussionBuilder(GHTeam team, String title) {
        this.team = team;
        this.builder = team.root.createRequest();
        builder.with("title", title);
    }

    /**
     * Title for this discussion.
     *
     * @param title
     *            title of discussion
     * @return a builder to continue with building
     */
    public GHDiscussionBuilder title(String title) {
        this.builder.with("title", title);
        return this;
    }

    /**
     * Body content for this discussion.
     *
     * @param body
     *            title of discussion
     * @return a builder to continue with building
     */
    public GHDiscussionBuilder body(String body) {
        this.builder.with("body", body);
        return this;
    }

    /**
     * Creates a discussion with all the parameters.
     *
     * @return the gh discussion
     * @throws IOException
     *             if discussion cannot be created
     */
    public GHDiscussion create() throws IOException {
        return builder.method("POST")
                .withUrlPath("/orgs/" + team.getOrganization().getName() + "/teams/" + team.getSlug() + "/discussions")
                .fetch(GHDiscussion.class)
                .wrapUp(team.root);
    }
}
