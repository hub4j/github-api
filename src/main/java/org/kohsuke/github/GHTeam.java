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
public class GHTeam {
    private String name,permission;
    private int id;
    private GHOrganization organization; // populated by GET /user/teams where Teams+Orgs are returned together

    protected /*final*/ GHOrganization org;

    /*package*/ GHTeam wrapUp(GHOrganization owner) {
        this.org = owner;
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

    public String getName() {
        return name;
    }

    public String getPermission() {
        return permission;
    }

    public int getId() {
        return id;
    }

    /**
     * Retrieves the current members.
     */
    public PagedIterable<GHUser> listMembers() throws IOException {
        return new PagedIterable<GHUser>() {
            public PagedIterator<GHUser> iterator() {
                return new PagedIterator<GHUser>(org.root.retrieve().asIterator(api("/members"), GHUser[].class)) {
                    @Override
                    protected void wrapUp(GHUser[] page) {
                        GHUser.wrap(page, org.root);
                    }
                };
            }
        };
    }

    public Set<GHUser> getMembers() throws IOException {
        return Collections.unmodifiableSet(listMembers().asSet());
    }

    /**
     * Checks if this team has the specified user as a member.
     */
    public boolean hasMember(GHUser user) {
        try {
            org.root.retrieve().to("/teams/" + id + "/members/"  + user.getLogin());
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
        return new PagedIterable<GHRepository>() {
            public PagedIterator<GHRepository> iterator() {
                return new PagedIterator<GHRepository>(org.root.retrieve().asIterator(api("/repos"), GHRepository[].class)) {
                    @Override
                    protected void wrapUp(GHRepository[] page) {
                        for (GHRepository r : page)
                            r.wrap(org.root);
                    }
                };
            }
        };
    }

    /**
     * Adds a member to the team.
     *
     * The user will be invited to the organization if required.
     *
     * @since 1.59
     */
    public void add(GHUser u) throws IOException {
        org.root.retrieve().method("PUT").to(api("/memberships/" + u.getLogin()), null);
    }

    /**
     * Removes a member to the team.
     */
    public void remove(GHUser u) throws IOException {
        org.root.retrieve().method("DELETE").to(api("/members/" + u.getLogin()), null);
    }

    public void add(GHRepository r) throws IOException {
        org.root.retrieve().method("PUT").to(api("/repos/" + r.getOwnerName() + '/' + r.getName()), null);
    }

    public void remove(GHRepository r) throws IOException {
        org.root.retrieve().method("DELETE").to(api("/repos/" + r.getOwnerName() + '/' + r.getName()), null);
    }

    private String api(String tail) {
        return "/teams/"+id+tail;
    }

    public GHOrganization getOrganization() {
      return org;
    }
}
