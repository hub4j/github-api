package org.kohsuke.github;

import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import static org.kohsuke.github.Previews.INERTIA;

/**
 * The type GHProjectCard.
 *
 * @author Gunnar Skjold
 */
public class GHProjectCard extends GHObject {
    private GitHub root;
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
    public GHProjectCard wrap(GitHub root) {
        this.root = root;
        return this;
    }

    /**
     * Wrap gh project card.
     *
     * @param column
     *            the column
     * @return the gh project card
     */
    public GHProjectCard wrap(GHProjectColumn column) {
        this.column = column;
        this.project = column.project;
        this.root = column.root;
        return this;
    }

    /**
     * Gets root.
     *
     * @return the root
     */
    public GitHub getRoot() {
        return root;
    }

    /**
     * Gets project.
     *
     * @return the project
     * @throws IOException
     *             the io exception
     */
    public GHProject getProject() throws IOException {
        if (project == null) {
            try {
                project = root.retrieve().to(getProjectUrl().getPath(), GHProject.class).wrap(root);
            } catch (FileNotFoundException e) {
                return null;
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
    public GHProjectColumn getColumn() throws IOException {
        if (column == null) {
            try {
                column = root.retrieve().to(getColumnUrl().getPath(), GHProjectColumn.class).wrap(root);
            } catch (FileNotFoundException e) {
                return null;
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
                return root.retrieve().to(getContentUrl().getPath(), GHPullRequest.class).wrap(root);
            } else {
                return root.retrieve().to(getContentUrl().getPath(), GHIssue.class).wrap(root);
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
    public GHUser getCreator() {
        return creator;
    }

    /**
     * Gets content url.
     *
     * @return the content url
     */
    public URL getContentUrl() {
        return GitHub.parseURL(content_url);
    }

    /**
     * Gets project url.
     *
     * @return the project url
     */
    public URL getProjectUrl() {
        return GitHub.parseURL(project_url);
    }

    /**
     * Gets column url.
     *
     * @return the column url
     */
    public URL getColumnUrl() {
        return GitHub.parseURL(column_url);
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
        new Requester(root).withPreview(INERTIA)._with(key, value).method("PATCH").to(getApiRoute());
    }

    /**
     * Gets api route.
     *
     * @return the api route
     */
    protected String getApiRoute() {
        return String.format("/projects/columns/cards/%d", id);
    }

    /**
     * Delete.
     *
     * @throws IOException
     *             the io exception
     */
    public void delete() throws IOException {
        new Requester(root).withPreview(INERTIA).method("DELETE").to(getApiRoute());
    }
}
