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
    private String number, body, title, htmlUrl;

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

    /**
     * The number of this discussion.
     *
     * @return the number
     */
    public String getNumber() {
        return number;
    }

    /**
     * Sets body.
     *
     * @param body
     *            the body
     * @throws IOException
     *             the io exception
     */
    public void setBody(String body) throws IOException {
        edit("body", body);
    }

    /**
     * Sets title.
     *
     * @param title
     *            the title
     * @throws IOException
     *             the io exception
     */
    public void setTitle(String title) throws IOException {
        edit("title", title);
    }

    /**
     * Edit the discussion
     *
     * @param key
     *            the key
     * @param value
     *            the value
     * @throws IOException
     *             the io exception
     */
    private void edit(String key, Object value) throws IOException {
        root.createRequest()
                .method("PATCH")
                // .withPreview(INERTIA)
                .with(key, value)
                .withUrlPath("/orgs/" + team.getOrganization().getLogin() + "/teams/" + team.getSlug() + "/discussions/"
                        + number)
                .send();
    }

    /**
     * Delete the discussion
     *
     * @throws IOException
     *             the io exception
     */
    public void delete(String number) throws IOException {
        root.createRequest()
                // .withPreview(INERTIA)
                .method("DELETE")
                .withUrlPath("/orgs/" + team.getOrganization().getLogin() + "/teams/" + team.getSlug() + "/discussions/"
                        + number)
                .send();
    }
}
