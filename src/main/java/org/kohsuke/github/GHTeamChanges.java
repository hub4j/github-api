package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.kohsuke.github.GHRepository.GHRepoPermission;
import org.kohsuke.github.GHTeam.Privacy;
import org.kohsuke.github.internal.EnumUtils;

/**
 * Changes made to a team.
 *
 * @see <a href=
 *      "https://docs.github.com/en/webhooks/webhook-events-and-payloads?actionType=edited#team">team
 *      event edited action</a>
 */
@SuppressFBWarnings(value = { "UWF_UNWRITTEN_FIELD" }, justification = "JSON API")
public class GHTeamChanges {

    private FromString description;
    private FromString name;
    private FromPrivacy privacy;
    private FromRepository repository;

    /**
     * Gets changes to description.
     *
     * @return changes to description.
     */
    public FromString getDescription() {
        return description;
    }

    /**
     * Gets changes to name.
     *
     * @return changes to name.
     */
    public FromString getName() {
        return name;
    }

    /**
     * Gets changes to privacy.
     *
     * @return changes to privacy.
     */
    public FromPrivacy getPrivacy() {
        return privacy;
    }

    /**
     * Gets changes for repository events.
     *
     * @return changes for repository events.
     */
    public FromRepository getRepository() {
        return repository;
    }

    /**
     * Changes made to a string value.
     */
    public static class FromString {

        private String from;

        /**
         * Gets the from.
         *
         * @return the from
         */
        public String getFrom() {
            return from;
        }
    }

    /**
     * Changes made to privacy.
     */
    public static class FromPrivacy {

        private String from;

        /**
         * Gets the from.
         *
         * @return the from
         */
        public Privacy getFrom() {
            return EnumUtils.getNullableEnumOrDefault(Privacy.class, from, Privacy.UNKNOWN);
        }
    }

    /**
     * Changes made for repository events.
     */
    public static class FromRepository {

        private FromRepositoryPermissions permissions;

        /**
         * Gets the changes to permissions.
         *
         * @return the changes to permissions
         */
        public FromRepositoryPermissions getPermissions() {
            return permissions;
        }
    }

    /**
     * Changes made to permissions.
     */
    public static class FromRepositoryPermissions {

        private GHRepoPermission from;

        /**
         * Has pull access boolean.
         *
         * @return the boolean
         */
        public boolean hadPullAccess() {
            return from != null && from.pull;
        }

        /**
         * Has push access boolean.
         *
         * @return the boolean
         */
        public boolean hadPushAccess() {
            return from != null && from.push;
        }

        /**
         * Has admin access boolean.
         *
         * @return the boolean
         */
        public boolean hadAdminAccess() {
            return from != null && from.admin;
        }
    }
}
