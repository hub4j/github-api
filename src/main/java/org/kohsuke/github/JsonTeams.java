package org.kohsuke.github;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Kohsuke Kawaguchi
 */
class JsonTeams {
    public List<GHTeam> teams;

    Map<String, GHTeam> toMap(GHOrganization org) {
        Map<String, GHTeam> r = new TreeMap<String, GHTeam>();
        for (GHTeam t : teams) {
            t.org = org;
            r.put(t.getName(),t);
        }
        return r;
    }
}
