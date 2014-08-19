package org.kohsuke.github;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
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
    public Set<GHUser> getMembers() throws IOException {
        return new HashSet<GHUser>(Arrays.asList(GHUser.wrap(org.root.retrieve().to(api("/members"), GHUser[].class), org.root)));
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
        GHRepository[] repos = org.root.retrieve().to(api("/repos"), GHRepository[].class);
        Map<String,GHRepository> m = new TreeMap<String, GHRepository>();
        for (GHRepository r : repos) {
            m.put(r.getName(),r.wrap(org.root));
        }
        return m;
    }

    /**
     * Adds a member to the team.
     */
    public void add(GHUser u) throws IOException {
        org.root.retrieve().method("PUT").to(api("/members/" + u.getLogin()), null);
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
