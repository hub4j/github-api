package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * An external group available in a GitHub organization.
 *
 * @author Miguel Esteban Gutiérrez
 */
public class GHExternalGroup extends GitHubInteractiveObject implements Refreshable {

    /**
     * A reference of a team linked to an external group
     *
     * @author Miguel Esteban Gutiérrez
     */
    public static class GHLinkedTeam {

        /**
         * The identifier of the team
         */
        @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Field comes from JSON deserialization")
        private long teamId;

        /**
         * The name of the team
         */
        @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Field comes from JSON deserialization")
        private String teamName;

        /**
         * Get the linked team identifier
         *
         * @return the id
         */
        public long getId() {
            return teamId;
        }

        /**
         * Get the linked team name
         *
         * @return the name
         */
        public String getName() {
            return teamName;
        }

    }

    /**
     * A reference of an external member linked to an external group
     */
    public static class GHLinkedExternalMember {

        /**
         * The internal user ID of the identity
         */
        @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Field comes from JSON deserialization")
        private long memberId;

        /**
         * The handle/login for the user
         */
        @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Field comes from JSON deserialization")
        private String memberLogin;

        /**
         * The user display name/profile name
         */
        @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Field comes from JSON deserialization")
        private String memberName;

        /**
         * The email attached to the user
         */
        @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Field comes from JSON deserialization")
        private String memberEmail;

        /**
         * Get the linked member identifier
         *
         * @return the id
         */
        public long getId() {
            return memberId;
        }

        /**
         * Get the linked member login
         *
         * @return the login
         */
        public String getLogin() {
            return memberLogin;
        }

        /**
         * Get the linked member name
         *
         * @return the name
         */
        public String getName() {
            return memberName;
        }

        /**
         * Get the linked member email
         *
         * @return the email
         */
        public String getEmail() {
            return memberEmail;
        }

    }

    /**
     * The identifier of the external group
     */
    @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Field comes from JSON deserialization")
    private long groupId;

    /**
     * The name of the external group
     */
    @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Field comes from JSON deserialization")
    private String groupName;

    /**
     * The date when the group was last updated at
     */
    @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Field comes from JSON deserialization")
    private String updatedAt;

    /**
     * The teams linked to this group
     */
    @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Field comes from JSON deserialization")
    private List<GHLinkedTeam> teams;

    /**
     * The external members linked to this group
     */
    @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Field comes from JSON deserialization")
    private List<GHLinkedExternalMember> members;

    GHExternalGroup() {
        this.teams = Collections.emptyList();
        this.members = Collections.emptyList();
    }

    private GHOrganization organization;

    /**
     * Wrap up.
     *
     * @param owner
     *            the owner
     */
    GHExternalGroup wrapUp(final GHOrganization owner) {
        this.organization = owner;
        return this;
    }

    /**
     * Wrap up.
     *
     * @param root
     *            the root
     */
    void wrapUp(final GitHub root) { // auto-wrapUp when organization is known from GET /orgs/{org}/external-groups
        wrapUp(organization);
    }

    /**
     * Gets organization.
     *
     * @return the organization
     * @throws IOException
     *             the io exception
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHOrganization getOrganization() throws IOException {
        return organization;
    }

    /**
     * Get the external group id.
     *
     * @return the id
     */
    public long getId() {
        return groupId;
    }

    /**
     * Get the external group name.
     *
     * @return the name
     */
    public String getName() {
        return groupName;
    }

    /**
     * Get the external group last update date.
     *
     * @return the date
     */
    public Date getUpdatedAt() {
        return GitHubClient.parseDate(updatedAt);
    }

    /**
     * Get the teams linked to this group.
     *
     * @return the teams
     */
    public List<GHLinkedTeam> getTeams() {
        return Collections.unmodifiableList(teams);
    }

    /**
     * Get the external members linked to this group.
     *
     * @return the external members
     */
    public List<GHLinkedExternalMember> getMembers() {
        return Collections.unmodifiableList(members);
    }

    /**
     * Refresh.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void refresh() throws IOException {
        root().createRequest().withUrlPath(api("")).fetchInto(this).wrapUp(root());
    }

    private String api(final String tail) {
        return "/orgs/" + organization.getLogin() + "/external-group/" + getId() + tail;
    }

}
