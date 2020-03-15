package org.kohsuke.github;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * A team in GitHub organization.
 *
 * @author Kohsuke Kawaguchi
 */
public class GHTeam implements Refreshable {
    private String name;
    private String permission;
    private String slug;
    private String description;
    private Privacy privacy;

    private int id;
    private GHOrganization organization; // populated by GET /user/teams where Teams+Orgs are returned together

    protected /* final */ GitHub root;

    public enum Privacy {
        SECRET, // only visible to organization owners and members of this team.
        CLOSED // visible to all members of this organization.
    }

    /**
     * Member's role in a team
     */
    public enum Role {
        /**
         * A normal member of the team
         */
        MEMBER,
        /**
         * Able to add/remove other team members, promote other team members to team maintainer, and edit the team's
         * name and description.
         */
        MAINTAINER
    }

    GHTeam wrapUp(GHOrganization owner) {
        this.organization = owner;
        this.root = owner.root;
        return this;
    }

    GHTeam wrapUp(GitHub root) { // auto-wrapUp when organization is known from GET /user/teams
        this.organization.wrapUp(root);
        return wrapUp(organization);
    }

    static GHTeam[] wrapUp(GHTeam[] teams, GHPullRequest owner) {
        for (GHTeam t : teams) {
            t.root = owner.root;
        }
        return teams;
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
     * Gets permission.
     *
     * @return the permission
     */
    public String getPermission() {
        return permission;
    }

    /**
     * Gets slug.
     *
     * @return the slug
     */
    public String getSlug() {
        return slug;
    }

    /**
     * Gets description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the privacy state.
     *
     * @return the privacy state.
     */
    public Privacy getPrivacy() {
        return privacy;
    }

    /**
     * Sets description.
     *
     * @param description
     *            the description
     * @throws IOException
     *             the io exception
     */
    public void setDescription(String description) throws IOException {
        root.createRequest().method("PATCH").with("description", description).withUrlPath(api("")).send();
    }

    /**
     * Updates the team's privacy setting.
     *
     * @param privacy
     *            the privacy
     * @throws IOException
     *             the io exception
     */
    public void setPrivacy(Privacy privacy) throws IOException {
        root.createRequest().method("PATCH").with("privacy", privacy).withUrlPath(api("")).send();
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * Retrieves the current members.
     *
     * @return the paged iterable
     * @throws IOException
     *             the io exception
     */
    public PagedIterable<GHUser> listMembers() throws IOException {
        return root.createRequest().withUrlPath(api("/members")).toIterable(GHUser[].class, item -> item.wrapUp(root));
    }

    /**
     * Gets members.
     *
     * @return the members
     * @throws IOException
     *             the io exception
     */
    public Set<GHUser> getMembers() throws IOException {
        return listMembers().toSet();
    }

    /**
     * Checks if this team has the specified user as a member.
     *
     * @param user
     *            the user
     * @return the boolean
     */
    public boolean hasMember(GHUser user) {
        try {
            root.createRequest().withUrlPath("/teams/" + id + "/members/" + user.getLogin()).send();
            return true;
        } catch (IOException ignore) {
            return false;
        }
    }

    /**
     * Gets repositories.
     *
     * @return the repositories
     * @throws IOException
     *             the io exception
     */
    public Map<String, GHRepository> getRepositories() throws IOException {
        Map<String, GHRepository> m = new TreeMap<String, GHRepository>();
        for (GHRepository r : listRepositories()) {
            m.put(r.getName(), r);
        }
        return m;
    }

    /**
     * List repositories paged iterable.
     *
     * @return the paged iterable
     */
    public PagedIterable<GHRepository> listRepositories() {
        return root.createRequest()
                .withUrlPath(api("/repos"))
                .toIterable(GHRepository[].class, item -> item.wrap(root));
    }

    /**
     * Adds a member to the team.
     * <p>
     * The user will be invited to the organization if required.
     *
     * @param u
     *            the u
     * @throws IOException
     *             the io exception
     * @since 1.59
     */
    public void add(GHUser u) throws IOException {
        root.createRequest().method("PUT").withUrlPath(api("/memberships/" + u.getLogin())).send();
    }

    /**
     * Adds a member to the team
     * <p>
     * The user will be invited to the organization if required.
     *
     * @param user
     *            github user
     * @param role
     *            role for the new member
     * @throws IOException
     *             the io exception
     */
    public void add(GHUser user, Role role) throws IOException {
        root.createRequest()
                .method("PUT")
                .with("role", role)
                .withUrlPath(api("/memberships/" + user.getLogin()))
                .send();
    }

    /**
     * Removes a member to the team.
     *
     * @param u
     *            the u
     * @throws IOException
     *             the io exception
     */
    public void remove(GHUser u) throws IOException {
        root.createRequest().method("DELETE").withUrlPath(api("/members/" + u.getLogin())).send();
    }

    /**
     * Add.
     *
     * @param r
     *            the r
     * @throws IOException
     *             the io exception
     */
    public void add(GHRepository r) throws IOException {
        add(r, null);
    }

    /**
     * Add.
     *
     * @param r
     *            the r
     * @param permission
     *            the permission
     * @throws IOException
     *             the io exception
     */
    public void add(GHRepository r, GHOrganization.Permission permission) throws IOException {
        root.createRequest()
                .method("PUT")
                .with("permission", permission)
                .withUrlPath(api("/repos/" + r.getOwnerName() + '/' + r.getName()))
                .send();
    }

    /**
     * Remove.
     *
     * @param r
     *            the r
     * @throws IOException
     *             the io exception
     */
    public void remove(GHRepository r) throws IOException {
        root.createRequest().method("DELETE").withUrlPath(api("/repos/" + r.getOwnerName() + '/' + r.getName())).send();
    }

    /**
     * Deletes this team.
     *
     * @throws IOException
     *             the io exception
     */
    public void delete() throws IOException {
        root.createRequest().method("DELETE").withUrlPath(api("")).send();
    }

    private String api(String tail) {
        return "/teams/" + id + tail;
    }

    /**
     * Gets organization.
     *
     * @return the organization
     * @throws IOException
     *             the io exception
     */
    public GHOrganization getOrganization() throws IOException {
        refresh(organization);
        return organization;
    }

    @Override
    public void refresh() throws IOException {
        root.createRequest().withUrlPath(api("")).fetchInto(this).wrapUp(root);
    }
}
