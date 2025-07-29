package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Changes made to a team.
 */
@SuppressFBWarnings(value = { "UWF_UNWRITTEN_FIELD" }, justification = "JSON API")
public class GHMemberChanges {

    /**
     * Changes to role name.
     */
    public static class FromRoleName {

        private String to;

        /**
         * Create default FromRoleName instance
         */
        public FromRoleName() {
        }

        /**
         * Gets the to.
         *
         * @return the to
         */
        public String getTo() {
            return to;
        }
    }

    /**
     * Changes to permission.
     */
    public static class FromToPermission {

        private String from;

        private String to;

        /**
         * Create default FromToPermission instance
         */
        public FromToPermission() {
        }

        /**
         * Gets the from.
         *
         * Cannot use {@link GHOrganization.Permission#ADMIN} due to messy underlying design.
         *
         * @return the from
         */
        public String getFrom() {
            return from;
        }

        /**
         * Gets the to.
         *
         * Cannot use {@link GHOrganization.Permission#ADMIN} due to messy underlying design.
         *
         * @return the to
         */
        public String getTo() {
            return to;
        }
    }

    private FromToPermission permission;

    private FromRoleName roleName;

    /**
     * Create default GHMemberChanges instance
     */
    public GHMemberChanges() {
    }

    /**
     * Get changes to permission.
     *
     * @return changes to permission
     */
    public FromToPermission getPermission() {
        return permission;
    }

    /**
     * Get changes to the role name.
     * <p>
     * Apparently, it is recommended to use this rather than permission if defined. But it will only be defined when
     * adding and not when editing.
     *
     * @return changes to role name
     */
    public FromRoleName getRoleName() {
        return roleName;
    }
}
