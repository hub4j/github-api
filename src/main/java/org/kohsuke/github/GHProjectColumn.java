package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

// TODO: Auto-generated Javadoc
/**
 * The type GHProjectColumn.
 *
 * @author Gunnar Skjold
 */
public class GHProjectColumn extends GHObject {

    /** The project. */
    protected GHProject project;

    private String name;
    private String project_url;

    /**
     * Wrap gh project column.
     *
     * @param root
     *            the root
     * @return the gh project column
     */
    GHProjectColumn lateBind(GitHub root) {
        return this;
    }

    /**
     * Wrap gh project column.
     *
     * @param project
     *            the project
     * @return the gh project column
     */
    GHProjectColumn lateBind(GHProject project) {
        this.project = project;
        return lateBind(project.root());
    }

    /**
     * Gets project.
     *
     * @return the project
     * @throws IOException
     *             the io exception
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHProject getProject() throws IOException {
        if (project == null) {
            try {
                project = root().createRequest().withUrlPath(getProjectUrl().getPath()).fetch(GHProject.class);
            } catch (FileNotFoundException e) {
            }
        }
        return project;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets project url.
     *
     * @return the project url
     */
    public URL getProjectUrl() {
        return GitHubClient.parseURL(project_url);
    }

    /**
     * Sets name.
     *
     * @param name
     *            the name
     * @throws IOException
     *             the io exception
     */
    public void setName(String name) throws IOException {
        edit("name", name);
    }

    private void edit(String key, Object value) throws IOException {
        root().createRequest().method("PATCH").with(key, value).withUrlPath(getApiRoute()).send();
    }

    /**
     * Gets api route.
     *
     * @return the api route
     */
    protected String getApiRoute() {
        return String.format("/projects/columns/%d", getId());
    }

    /**
     * Delete.
     *
     * @throws IOException
     *             the io exception
     */
    public void delete() throws IOException {
        root().createRequest().method("DELETE").withUrlPath(getApiRoute()).send();
    }

    /**
     * List cards paged iterable.
     *
     * @return the paged iterable
     * @throws IOException
     *             the io exception
     */
    public PagedIterable<GHProjectCard> listCards() throws IOException {
        final GHProjectColumn column = this;
        return root().createRequest()
                .withUrlPath(String.format("/projects/columns/%d/cards", getId()))
                .toIterable(GHProjectCard[].class, item -> item.lateBind(column));
    }

    /**
     * Create card gh project card.
     *
     * @param note
     *            the note
     * @return the gh project card
     * @throws IOException
     *             the io exception
     */
    public GHProjectCard createCard(String note) throws IOException {
        return root().createRequest()
                .method("POST")
                .with("note", note)
                .withUrlPath(String.format("/projects/columns/%d/cards", getId()))
                .fetch(GHProjectCard.class)
                .lateBind(this);
    }

    /**
     * Create card gh project card.
     *
     * @param issue
     *            the issue
     * @return the gh project card
     * @throws IOException
     *             the io exception
     */
    public GHProjectCard createCard(GHIssue issue) throws IOException {
        String contentType = issue instanceof GHPullRequest ? "PullRequest" : "Issue";
        return root().createRequest()
                .method("POST")
                .with("content_type", contentType)
                .with("content_id", issue.getId())
                .withUrlPath(String.format("/projects/columns/%d/cards", getId()))
                .fetch(GHProjectCard.class)
                .lateBind(this);
    }
}
