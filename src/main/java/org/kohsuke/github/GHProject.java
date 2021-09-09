/*
 * The MIT License
 *
 * Copyright 2018 Martin van Zijl.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;

import static org.kohsuke.github.internal.Previews.INERTIA;

/**
 * A GitHub project.
 *
 * @author Martin van Zijl
 * @see <a href="https://developer.github.com/v3/projects/">Projects</a>
 */
public class GHProject extends GHObject {
    protected GHObject owner;

    private String owner_url;
    private String html_url;
    private String name;
    private String body;
    private int number;
    private String state;
    private GHUser creator;

    @Override
    public URL getHtmlUrl() throws IOException {
        return GitHubClient.parseURL(html_url);
    }

    /**
     * Gets owner.
     *
     * @return the owner
     * @throws IOException
     *             the io exception
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHObject getOwner() throws IOException {
        if (owner == null) {
            try {
                if (owner_url.contains("/orgs/")) {
                    owner = root().createRequest()
                            .withUrlPath(getOwnerUrl().getPath())
                            .fetch(GHOrganization.class)
                            .wrapUp(root());
                } else if (owner_url.contains("/users/")) {
                    owner = root().createRequest()
                            .withUrlPath(getOwnerUrl().getPath())
                            .fetch(GHUser.class)
                            .wrapUp(root());
                } else if (owner_url.contains("/repos/")) {
                    String[] pathElements = getOwnerUrl().getPath().split("/");
                    owner = GHRepository.read(root(), pathElements[1], pathElements[2]);
                }
            } catch (FileNotFoundException e) {
                return null;
            }
        }
        return owner;
    }

    /**
     * Gets owner url.
     *
     * @return the owner url
     */
    public URL getOwnerUrl() {
        return GitHubClient.parseURL(owner_url);
    }

    /**
     * Gets node id.
     *
     * @deprecated Use {@link GHObject#getNodeId()}
     * @return the node id
     */
    @Deprecated
    public String getNode_id() {
        return getNodeId();
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
     * Gets body.
     *
     * @return the body
     */
    public String getBody() {
        return body;
    }

    /**
     * Gets number.
     *
     * @return the number
     */
    public int getNumber() {
        return number;
    }

    /**
     * Gets state.
     *
     * @return the state
     */
    public ProjectState getState() {
        return Enum.valueOf(ProjectState.class, state.toUpperCase(Locale.ENGLISH));
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
     * Wrap gh project.
     *
     * @param root
     *            the root
     * @return the gh project
     */
    @Deprecated
    public GHProject wrap(GitHub root) {
        throw new RuntimeException("Do not use this method.");
    }

    /**
     * Wrap gh project.
     *
     * @param root
     *            the root
     * @return the gh project
     */
    GHProject lateBind(GitHub root) {
        return this;
    }

    /**
     * Wrap gh project.
     *
     * @param repo
     *            the repo
     * @return the gh project
     */
    @Deprecated
    public GHProject wrap(GHRepository repo) {
        throw new RuntimeException("Do not use this method.");
    }

    /**
     * Wrap gh project.
     *
     * @param repo
     *            the repo
     * @return the gh project
     */
    GHProject lateBind(GHRepository repo) {
        this.owner = repo;
        return lateBind(repo.root());
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
        return "/projects/" + getId();
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
     * The enum ProjectState.
     */
    public enum ProjectState {
        OPEN, CLOSED
    }

    /**
     * Sets state.
     *
     * @param state
     *            the state
     * @throws IOException
     *             the io exception
     */
    public void setState(ProjectState state) throws IOException {
        edit("state", state.toString().toLowerCase());
    }

    /**
     * The enum ProjectStateFilter.
     */
    public static enum ProjectStateFilter {
        ALL, OPEN, CLOSED
    }

    /**
     * Set the permission level that all members of the project's organization will have on this project. Only
     * applicable for organization-owned projects.
     *
     * @param permission
     *            the permission
     * @throws IOException
     *             the io exception
     */
    public void setOrganizationPermission(GHPermissionType permission) throws IOException {
        edit("organization_permission", permission.toString().toLowerCase());
    }

    /**
     * Sets visibility of the project within the organization. Only applicable for organization-owned projects.
     *
     * @param isPublic
     *            the is public
     * @throws IOException
     *             the io exception
     */
    public void setPublic(boolean isPublic) throws IOException {
        edit("public", isPublic);
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

    /**
     * List columns paged iterable.
     *
     * @return the paged iterable
     * @throws IOException
     *             the io exception
     */
    public PagedIterable<GHProjectColumn> listColumns() throws IOException {
        final GHProject project = this;
        return root().createRequest()
                .withPreview(INERTIA)
                .withUrlPath(String.format("/projects/%d/columns", getId()))
                .toIterable(GHProjectColumn[].class, item -> item.lateBind(project));
    }

    /**
     * Create column gh project column.
     *
     * @param name
     *            the name
     * @return the gh project column
     * @throws IOException
     *             the io exception
     */
    public GHProjectColumn createColumn(String name) throws IOException {
        return root().createRequest()
                .method("POST")
                .withPreview(INERTIA)
                .with("name", name)
                .withUrlPath(String.format("/projects/%d/columns", getId()))
                .fetch(GHProjectColumn.class)
                .lateBind(this);
    }
}
