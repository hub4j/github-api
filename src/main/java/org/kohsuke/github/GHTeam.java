package org.kohsuke.github;

import java.io.IOException;
import java.net.URL;
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

    public String getName() {
        return name;
    }

    public String getPermission() {
        return permission;
    }

    public int getId() {
        return id;
    }

    public Set<GHUser> getMembers() throws IOException {
        return org.root.retrieveWithAuth(getApiURL("/members"),JsonUsersWithDetails.class).toSet(org.root);
    }

    private URL getApiURL(String tail) throws IOException {
        return org.root.getApiURL("/organizations/"+org.getLogin()+"/teams/"+id+tail);
    }
}
