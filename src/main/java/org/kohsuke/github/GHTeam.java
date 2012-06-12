package org.kohsuke.github;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
        return new HashSet<GHUser>(Arrays.asList(GHUser.wrap(org.root.retrieveWithAuth3(api("/members"),GHUser[].class),org.root)));
    }

    public Map<String,GHRepository> getRepositories() throws IOException {
        return org.root.retrieveWithAuth3(api("/repos"),JsonRepositories.class).wrap(org.root);
    }

    /**
     * Adds a member to the team.
     */
    public void add(GHUser u) throws IOException {
        org.root.retrieveWithAuth3(api("/members/"+u.getLogin()),null, "PUT");
    }

    /**
     * Removes a member to the team.
     */
    public void remove(GHUser u) throws IOException {
        org.root.retrieveWithAuth3(api("/members/"+u.getLogin()),null, "DELETE");
    }

    public void add(GHRepository r) throws IOException {
        org.root.retrieveWithAuth3(api("/repos/"+r.getOwnerName()+'/'+r.getName()),null, "PUT");
    }

    public void remove(GHRepository r) throws IOException {
        org.root.retrieveWithAuth3(api("/repos/"+r.getOwnerName()+'/'+r.getName()),null, "DELETE");
    }

    private String api(String tail) {
        return "/teams/"+id+tail;
    }
}
