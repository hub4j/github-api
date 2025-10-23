package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * A list of external groups.
 *
 * @author Miguel Esteban Gutiérrez
 */
class GHExternalGroupPage implements GitHubPage<GHExternalGroup> {

    private static final GHExternalGroup[] GH_EXTERNAL_GROUPS = new GHExternalGroup[0];

    private GHExternalGroup[] groups;

    GHExternalGroupPage() {
        this(GH_EXTERNAL_GROUPS);
    }

    GHExternalGroupPage(GHExternalGroup[] groups) {
        this.groups = groups;
    }

    /**
     * Gets the groups.
     *
     * @return the groups
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHExternalGroup[] getGroups() {
        return groups;
    }

    @Override
    public GHExternalGroup[] getItems() {
        return getGroups();
    }
}
