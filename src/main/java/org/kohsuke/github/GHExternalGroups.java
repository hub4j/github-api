package org.kohsuke.github;

import java.util.Collections;
import java.util.List;

/**
 * A list of external groups.
 *
 * @author Miguel Esteban Gutiérrez
 */
public class GHExternalGroups {

    private List<GHExternalGroup> groups;

    GHExternalGroups() {
        this.groups = Collections.emptyList();
    }

    /**
     * Gets the groups.
     *
     * @return the groups
     */
    public List<GHExternalGroup> getGroups() {
        return Collections.unmodifiableList(groups);
    }

}
