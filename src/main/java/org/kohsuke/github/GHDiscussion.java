package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JacksonInject;

import java.io.IOException;
import java.net.URL;

/**
 * A discussion in GitHub Team.
 *
 * @author Charles Moulliard
 * @see <a href="https://developer.github.com/v3/teams/discussions">GitHub Team Discussions</a>
 */
public class GHDiscussion extends GHObject {

    @JacksonInject
    private GitHub root;
    private GHOrganization organization;
    private GHTeam team;
    private String body, title, htmlUrl;

    @Override
    public URL getHtmlUrl() throws IOException {
        return GitHubClient.parseURL(htmlUrl);
    }

    public GHDiscussion wrapUp(GHTeam team) throws IOException {
        this.organization = team.getOrganization();
        this.team = team;
        return this;
    }

    public GHDiscussion wrapUp(GitHub root) {
        this.root = root;
        return this;
    }

    public GHDiscussion wrapUp(GHOrganization owner) {
        this.organization = owner;
        return this;
    }

    /**
     * Get the title of the discussion.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * The description of this discussion.
     *
     * @return the body
     */
    public String getBody() {
        return body;
    }
}
