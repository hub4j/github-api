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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;

import static org.kohsuke.github.Previews.INERTIA;

/**
 * A GitHub project.
 * @see https://developer.github.com/v3/projects/
 * @author Martin van Zijl
 */
public class GHProject extends GHObject {
    protected GitHub root;
    protected GHObject owner;

    private String owner_url;
    private String html_url;
    private String node_id;
    private String name;
    private String body;
    private int number;
    private String state;
    private GHUser creator;

    @Override
    public URL getHtmlUrl() throws IOException {
        return GitHub.parseURL(html_url);
    }

    public GitHub getRoot() {
        return root;
    }

    public GHObject getOwner() throws IOException {
        if(owner == null) {
            try {
                if(owner_url.contains("/orgs/")) {
                    owner = root.retrieve().to(getOwnerUrl().getPath(), GHOrganization.class).wrapUp(root);
                } else if(owner_url.contains("/users/")) {
                    owner = root.retrieve().to(getOwnerUrl().getPath(), GHUser.class).wrapUp(root);
                } else if(owner_url.contains("/repos/")) {
                    owner = root.retrieve().to(getOwnerUrl().getPath(), GHRepository.class).wrap(root);
                }
            } catch (FileNotFoundException e) {
                return null;
            }
        }
        return owner;
    }

    public URL getOwnerUrl() {
        return GitHub.parseURL(owner_url);
    }

    public String getNode_id() {
        return node_id;
    }

    public String getName() {
        return name;
    }

    public String getBody() {
        return body;
    }

    public int getNumber() {
        return number;
    }

    public ProjectState getState() {
        return Enum.valueOf(ProjectState.class, state.toUpperCase(Locale.ENGLISH));
    }

    public GHUser getCreator() {
        return creator;
    }

    public GHProject wrap(GHRepository repo) {
        this.owner = repo;
        this.root = repo.root;
        return this;
    }

    public GHProject wrap(GitHub root) {
        this.root = root;
        return this;
    }

    private void edit(String key, Object value) throws IOException {
        new Requester(root).withPreview(INERTIA)._with(key, value).method("PATCH").to(getApiRoute());
    }

    protected String getApiRoute() {
        return "/projects/" + id;
    }

    public void setName(String name) throws IOException {
        edit("name", name);
    }

    public void setBody(String body) throws IOException {
        edit("body", body);
    }

    public enum ProjectState {
        OPEN,
        CLOSED
    }

    public void setState(ProjectState state) throws IOException {
        edit("state", state.toString().toLowerCase());
    }

    public static enum ProjectStateFilter {
        ALL,
        OPEN,
        CLOSED
    }

    /**
     * Set the permission level that all members of the project's organization will have on this project.
     * Only applicable for organization-owned projects.
     */
    public void setOrganizationPermission(GHPermissionType permission) throws IOException {
        edit("organization_permission", permission.toString().toLowerCase());
    }

    /**
     * Sets visibility of the project within the organization.
     * Only applicable for organization-owned projects.
     */
    public void setPublic(boolean isPublic) throws IOException {
        edit("public", isPublic);
    }

    public void delete() throws IOException {
        new Requester(root).withPreview(INERTIA).method("DELETE").to(getApiRoute());
    }

    public PagedIterable<GHProjectColumn> listColumns() throws IOException {
        final GHProject project = this;
        return new PagedIterable<GHProjectColumn>() {
            public PagedIterator<GHProjectColumn> _iterator(int pageSize) {
                return new PagedIterator<GHProjectColumn>(root.retrieve().withPreview(INERTIA)
                        .asIterator(String.format("/projects/%d/columns", id), GHProjectColumn[].class, pageSize)) {
                    @Override
                    protected void wrapUp(GHProjectColumn[] page) {
                        for (GHProjectColumn c : page)
                            c.wrap(project);
                    }
                };
            }
        };
    }

    public GHProjectColumn createColumn(String name) throws IOException {
        return root.retrieve().method("POST")
                .withPreview(INERTIA)
                .with("name", name)
                .to(String.format("/projects/%d/columns", id), GHProjectColumn.class).wrap(this);
    }
}