package org.kohsuke.github;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    Set<GHTeam> toSet(GHOrganization org) {
        Set<GHTeam> r = new HashSet<GHTeam>();
        for (GHTeam t : teams) {
            t.org = org;
            r.add(t);
        }
        return r;
    }
}
