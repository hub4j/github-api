package org.kohsuke.github;

/**
 * @author Kohsuke Kawaguchi
 */
class JsonTeam {
    public GHTeam team;

    GHTeam wrap(GHOrganization org) {
        team.org = org;
        return team;
    }
}
