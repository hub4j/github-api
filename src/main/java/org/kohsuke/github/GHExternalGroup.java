package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonProperty;
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
        @JsonProperty("team_id")
        @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Field comes from JSON deserialization")
        private long id;

        /**
         * The name of the team
         */
        @JsonProperty("team_name")
        @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Field comes from JSON deserialization")
        private String name;

        /**
         * Get the linked team identifier
         *
         * @return the id
         */
        public long getId() {
            return id;
        }

        /**
         * Get the linked team name
         *
         * @return the name
         */
        public String getName() {
            return name;
        }

    }

    /**
     * A reference of an external member linked to an external group
     */
    public static class GHLinkedExternalMember {

        /**
         * The internal user ID of the identity
         */
        @JsonProperty("member_id")
        @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Field comes from JSON deserialization")
        private long id;

        /**
         * The handle/login for the user
         */
        @JsonProperty("member_login")
        @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Field comes from JSON deserialization")
        private String login;

        /**
         * The user display name/profile name
         */
        @JsonProperty("member_name")
        @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Field comes from JSON deserialization")
        private String name;

        /**
         * The email attached to the user
         */
        @JsonProperty("member_email")
        @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Field comes from JSON deserialization")
        private String email;

        /**
         * Get the linked member identifier
         *
         * @return the id
         */
        public long getId() {
            return id;
        }

        /**
         * Get the linked member login
         *
         * @return the login
         */
        public String getLogin() {
            return login;
        }

        /**
         * Get the linked member name
         *
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * Get the linked member email
         *
         * @return the email
         */
        public String getEmail() {
            return email;
        }

    }

    /**
     * The identifier of the external group
     */
    @JsonProperty("group_id")

    @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Field comes from JSON deserialization")
    private long id;

    /**
     * The name of the external group
     */
    @JsonProperty("group_name")
    @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Field comes from JSON deserialization")
    private String name;

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
        return id;
    }

    /**
     * Get the external group name.
     *
     * @return the name
     */
    public String getName() {
        return name;
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
