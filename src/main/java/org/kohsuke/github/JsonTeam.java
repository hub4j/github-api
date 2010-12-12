package org.kohsuke.github;

/**
 * @author Kohsuke Kawaguchi
 */
public class JsonTeam {
    public GHTeam team;

    GHTeam wrap(GHOrganization org) {
        team.org = org;
        return team;
    }
}
