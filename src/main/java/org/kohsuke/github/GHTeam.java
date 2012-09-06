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

    protected /*final*/ GHOrganization org;

    /*package*/ GHTeam wrapUp(GHOrganization owner) {
        this.org = owner;
        return this;
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
        return new HashSet<GHUser>(Arrays.asList(GHUser.wrap(org.root.retrieve().withCredential().to(api("/members"), GHUser[].class), org.root)));
    }

    public Map<String,GHRepository> getRepositories() throws IOException {
        GHRepository[] repos = org.root.retrieve().withCredential().to(api("/repos"), GHRepository[].class);
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
        org.root.retrieve().withCredential().method("PUT").to(api("/members/" + u.getLogin()), null);
    }

    /**
     * Removes a member to the team.
     */
    public void remove(GHUser u) throws IOException {
        org.root.retrieve().withCredential().method("DELETE").to(api("/members/" + u.getLogin()), null);
    }

    public void add(GHRepository r) throws IOException {
        org.root.retrieve().withCredential().method("PUT").to(api("/repos/" + r.getOwnerName() + '/' + r.getName()), null);
    }

    public void remove(GHRepository r) throws IOException {
        org.root.retrieve().withCredential().method("DELETE").to(api("/repos/" + r.getOwnerName() + '/' + r.getName()), null);
    }

    private String api(String tail) {
        return "/teams/"+id+tail;
    }
}
