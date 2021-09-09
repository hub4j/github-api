package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import static org.kohsuke.github.internal.Previews.INERTIA;

/**
 * The type GHProjectCard.
 *
 * @author Gunnar Skjold
 */
public class GHProjectCard extends GHObject {
    private GHProject project;
    private GHProjectColumn column;

    private String note;
    private GHUser creator;
    private String content_url, project_url, column_url;
    private boolean archived;

    public URL getHtmlUrl() throws IOException {
        return null;
    }

    /**
     * Wrap gh project card.
     *
     * @param root
     *            the root
     * @return the gh project card
     */
    @Deprecated
    public GHProjectCard wrap(GitHub root) {
        throw new RuntimeException("Do not use this method.");
    }

    /**
     * Wrap gh project card.
     *
     * @param root
     *            the root
     * @return the gh project card
     */
    GHProjectCard lateBind(GitHub root) {
        return this;
    }

    /**
     * Wrap gh project card.
     *
     * @param column
     *            the column
     * @return the gh project card
     */
    @Deprecated
    public GHProjectCard wrap(GHProjectColumn column) {
        throw new RuntimeException("Do not use this method.");
    }

    /**
     * Wrap gh project card.
     *
     * @param column
     *            the column
     * @return the gh project card
     */
    GHProjectCard lateBind(GHProjectColumn column) {
        this.column = column;
        this.project = column.project;
        return lateBind(column.root());
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
                project = root().createRequest()
                        .withUrlPath(getProjectUrl().getPath())
                        .fetch(GHProject.class)
                        .lateBind(root());
            } catch (FileNotFoundException e) {
            }
        }
        return project;
    }

    /**
     * Gets column.
     *
     * @return the column
     * @throws IOException
     *             the io exception
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHProjectColumn getColumn() throws IOException {
        if (column == null) {
            try {
                column = root().createRequest()
                        .withUrlPath(getColumnUrl().getPath())
                        .fetch(GHProjectColumn.class)
                        .lateBind(root());
            } catch (FileNotFoundException e) {
            }
        }
        return column;
    }

    /**
     * Gets content.
     *
     * @return the content
     * @throws IOException
     *             the io exception
     */
    public GHIssue getContent() throws IOException {
        if (StringUtils.isEmpty(content_url))
            return null;
        try {
            if (content_url.contains("/pulls")) {
                return root().createRequest()
                        .withUrlPath(getContentUrl().getPath())
                        .fetch(GHPullRequest.class)
                        .wrap(root());
            } else {
                return root().createRequest().withUrlPath(getContentUrl().getPath()).fetch(GHIssue.class).wrap(root());
            }
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    /**
     * Gets note.
     *
     * @return the note
     */
    public String getNote() {
        return note;
    }

    /**
     * Gets creator.
     *
     * @return the creator
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHUser getCreator() {
        return creator;
    }

    /**
     * Gets content url.
     *
     * @return the content url
     */
    public URL getContentUrl() {
        return GitHubClient.parseURL(content_url);
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
     * Gets column url.
     *
     * @return the column url
     */
    public URL getColumnUrl() {
        return GitHubClient.parseURL(column_url);
    }

    /**
     * Is archived boolean.
     *
     * @return the boolean
     */
    public boolean isArchived() {
        return archived;
    }

    /**
     * Sets note.
     *
     * @param note
     *            the note
     * @throws IOException
     *             the io exception
     */
    public void setNote(String note) throws IOException {
        edit("note", note);
    }

    /**
     * Sets archived.
     *
     * @param archived
     *            the archived
     * @throws IOException
     *             the io exception
     */
    public void setArchived(boolean archived) throws IOException {
        edit("archived", archived);
    }

    private void edit(String key, Object value) throws IOException {
        root().createRequest().method("PATCH").withPreview(INERTIA).with(key, value).withUrlPath(getApiRoute()).send();
    }

    /**
     * Gets api route.
     *
     * @return the api route
     */
    protected String getApiRoute() {
        return String.format("/projects/columns/cards/%d", getId());
    }

    /**
     * Delete.
     *
     * @throws IOException
     *             the io exception
     */
    public void delete() throws IOException {
        root().createRequest().withPreview(INERTIA).method("DELETE").withUrlPath(getApiRoute()).send();
    }
}
