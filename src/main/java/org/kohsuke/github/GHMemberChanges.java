package org.kohsuke.github;

import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.kohsuke.github.internal.EnumUtils;

/**
 * Changes made to a team.
 */
@SuppressFBWarnings(value = { "UWF_UNWRITTEN_FIELD" }, justification = "JSON API")
public class GHMemberChanges {

    private FromToPermission permission;

    private FromRoleName roleName;

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
        @WithBridgeMethods(value = GHOrganization.Permission.class, adapterMethod = "stringToOrgPermission")
        public String getFrom() {
            return from;
        }

        /**
         * Gets the to.
         *
         * @return the to
         */
        @WithBridgeMethods(value = GHOrganization.Permission.class, adapterMethod = "stringToOrgPermission")
        public String getTo() {
            return to;
        }

        @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "Bridge method of getFrom and getTo")
        private Object stringToOrgPermission(String permissionType, Class type) {
            switch (permissionType) {
                case "admin" :
                    return GHOrganization.Permission.ADMIN;
                case "none" :
                    return GHOrganization.Permission.UNKNOWN;
                case "read" :
                    return GHOrganization.Permission.PULL;
                case "write" :
                    return GHOrganization.Permission.PUSH;
                default :
                    return EnumUtils.getNullableEnumOrDefault(GHPermissionType.class, to, GHPermissionType.UNKNOWN);
            }
        }
    }

    /**
     * Changes to role name.
     */
    public static class FromRoleName {

        private String to;

        /**
         * Gets the to.
         *
         * @return the to
         */
        public String getTo() {
            return to;
        }
    }
}
