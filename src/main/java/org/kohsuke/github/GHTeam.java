package org.kohsuke.github;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * A team in GitHub organization.
 * 
 * @author Kohsuke Kawaguchi
 */
public class GHTeam extends GHObjectBase implements Refreshable {
    private String name,permission,slug,description;
    private int id;
    private GHOrganization organization; // populated by GET /user/teams where Teams+Orgs are returned together

    /** Member's role in a team */
    public enum Role {
        /**
         * A normal member of the team
         */
        MEMBER,
        /**
         * Able to add/remove other team members, promote other team members to team maintainer, and edit the team's name and description.
         */
        MAINTAINER
    }

    /*package*/ GHTeam wrapUp(GHOrganization owner) {
        this.organization = owner;
        this.root = owner.root;
        return this;
    }

    /*package*/ GHTeam wrapUp(GitHub root) { // auto-wrapUp when organization is known from GET /user/teams
      this.organization.wrapUp(root);
      return wrapUp(organization);
    }

    /*package*/ static GHTeam[] wrapUp(GHTeam[] teams, GHOrganization owner) {
        for (GHTeam t : teams) {
            t.wrapUp(owner);
        }
        return teams;
    }

    /*package*/ static GHTeam[] wrapUp(GHTeam[] teams, GHPullRequest owner) {
        for (GHTeam t : teams) {
            t.root = owner.root;
        }
        return teams;
    }

    public String getName() {
        return name;
    }

    public String getPermission() {
        return permission;
    }

    public String getSlug() {
        return slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) throws IOException {
        root.retrieve().method("PATCH")
                .with("description", description)
                .to(api(""));
    }

    public int getId() {
        return id;
    }

    /**
     * Retrieves the current members.
     */
    public PagedIterable<GHUser> listMembers() throws IOException {
        return root.retrieve()
            .asPagedIterable(
                api("/members"),
                GHUser[].class,
                item -> item.wrapUp(root) );
    }

    public Set<GHUser> getMembers() throws IOException {
        return Collections.unmodifiableSet(listMembers().asSet());
    }

    /**
     * Checks if this team has the specified user as a member.
     */
    public boolean hasMember(GHUser user) {
        try {
            root.retrieve().to("/teams/" + id + "/members/"  + user.getLogin());
            return true;
        } catch (IOException ignore) {
            return false;
        }
    }

    public Map<String,GHRepository> getRepositories() throws IOException {
        Map<String,GHRepository> m = new TreeMap<String, GHRepository>();
        for (GHRepository r : listRepositories()) {
            m.put(r.getName(), r);
        }
        return m;
    }

    public PagedIterable<GHRepository> listRepositories() {
        return root.retrieve()
            .asPagedIterable(
                api("/repos"),
                GHRepository[].class,
                item -> item.wrap(root) );
    }

    /**
     * Adds a member to the team.
     *
     * The user will be invited to the organization if required.
     *
     * @since 1.59
     */
    public void add(GHUser u) throws IOException {
        root.retrieve().method("PUT").to(api("/memberships/" + u.getLogin()), null);
    }

    /**
     * Adds a member to the team
     *
     * The user will be invited to the organization if required.
     *
     * @param user github user
     * @param role role for the new member
     *
     * @throws IOException
     */
    public void add(GHUser user, Role role) throws IOException {
        root.retrieve().method("PUT")
                .with("role", role)
                .to(api("/memberships/" + user.getLogin()), null);
    }

    /**
     * Removes a member to the team.
     */
    public void remove(GHUser u) throws IOException {
        root.retrieve().method("DELETE").to(api("/members/" + u.getLogin()), null);
    }

    public void add(GHRepository r) throws IOException {
        add(r,null);
    }

    public void add(GHRepository r, GHOrganization.Permission permission) throws IOException {
        root.retrieve().method("PUT")
                .with("permission", permission)
                .to(api("/repos/" + r.getOwnerName() + '/' + r.getName()), null);
    }

    public void remove(GHRepository r) throws IOException {
        root.retrieve().method("DELETE").to(api("/repos/" + r.getOwnerName() + '/' + r.getName()), null);
    }
    
    /**
     * Deletes this team.
     */
    public void delete() throws IOException {
        root.retrieve().method("DELETE").to(api(""));
    }

    private String api(String tail) {
        return "/teams/" + id + tail;
    }

    public GHOrganization getOrganization() throws IOException {
        refresh(organization);
        return organization;
    }

    @Override
    public void refresh() throws IOException {
        root.retrieve().to(api(""), this).wrapUp(root);
    }
}
