package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.kohsuke.github.internal.EnumUtils;

/**
 * Changes made to a team.
 */
@SuppressFBWarnings(value = { "UWF_UNWRITTEN_FIELD" }, justification = "JSON API")
public class GHMemberChanges {

    private FromToPermission permission;

    /**
     * Get changes to permission.
     *
     * @return changes to permission
     */
    public FromToPermission getPermission() {
        return permission;
    }

    /**
     * Changes to permission.
     */
    public static class FromToPermission {

        private String from;

        private String to;

        /**
         * Gets the from.
         *
         * @return the from
         */
        public GHOrganization.Permission getFrom() {
            return EnumUtils
                    .getNullableEnumOrDefault(GHOrganization.Permission.class, from, GHOrganization.Permission.UNKNOWN);
        }

        /**
         * Gets the to.
         *
         * @return the to
         */
        public GHOrganization.Permission getTo() {
            return EnumUtils
                    .getNullableEnumOrDefault(GHOrganization.Permission.class, to, GHOrganization.Permission.UNKNOWN);
        }
    }
}
