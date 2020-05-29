package org.kohsuke.github;

import java.io.IOException;
import java.net.URL;

/**
 * A discussion in GitHub Team.
 *
 * @author Charles Moulliard
 */
public class GHDiscussion extends GHObject {

    protected String body, title, html_url;

    @Override
    public URL getHtmlUrl() throws IOException {
        return GitHubClient.parseURL(html_url);
    }

    public GHDiscussion wrapUp(GitHub root) {
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
