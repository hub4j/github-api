package org.kohsuke.github;

import java.io.IOException;
import java.net.URL;

/**
 * A discussion in GitHub Team.
 *
 * @author Charles Moulliard
 */
public class GHDiscussion extends GHObject {

    protected GitHub root;
    protected GHOrganization organization;
    protected GHTeam team;
    protected String body, title, html_url;

    @Override
    public URL getHtmlUrl() throws IOException {
        return GitHubClient.parseURL(html_url);
    }

    public GHDiscussion wrapUp(GHTeam team) throws IOException {
        this.root = team.root;
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
        this.root = owner.root;
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
