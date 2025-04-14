package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.internal.EnumUtils;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import javax.annotation.Nonnull;

import static org.kohsuke.github.GitHubRequest.transformEnum;

// TODO: Auto-generated Javadoc
/**
 * A team in GitHub organization.
 *
 * @author Kohsuke Kawaguchi
 */
public class GHTeam extends GHObject implements Refreshable {

    /**
     * The Enum Privacy.
     */
    public enum Privacy {

        /** The closed. */
        // only visible to organization owners and members of this team.
        CLOSED,
        /** The secret. */
        SECRET, // visible to all members of this organization.
        /** Unknown privacy value */
        UNKNOWN
    }

    /**
     * Member's role in a team.
     */
    public enum Role {

        /**
         * Able to add/remove other team members, promote other team members to team maintainer, and edit the team's
         * name and description.
         */
        MAINTAINER,
        /** A normal member of the team. */
        MEMBER
    }

    /**
     * Path for external group-related operations
     */
    private static final String EXTERNAL_GROUPS = "/external-groups";
    private String description;
    private String htmlUrl;
    private String name;
    private GHOrganization organization; // populated by GET /user/teams where Teams+Orgs are returned together
    private String permission;

    private String privacy;

    private String slug;

    /**
     * Create default GHTeam instance
     */
    public GHTeam() {
    }

    /**
     * Add.
     *
     * @param r
     *            the r
     * @throws IOException
     *             the io exception
     */
    public void add(GHRepository r) throws IOException {
        add(r, (GHOrganization.RepositoryRole) null);
    }

    /**
     * Add.
     *
     * @param r
     *            the r
     * @param permission
     *            the permission
     * @throws IOException
     *             the io exception
     */
    public void add(GHRepository r, GHOrganization.RepositoryRole permission) throws IOException {
        root().createRequest()
                .method("PUT")
                .with("permission",
                        Optional.ofNullable(permission).map(GHOrganization.RepositoryRole::toString).orElse(null))
                .withUrlPath(api("/repos/" + r.getOwnerName() + '/' + r.getName()))
                .send();
    }

    /**
     * Adds a member to the team.
     * <p>
     * The user will be invited to the organization if required.
     *
     * @param u
     *            the u
     * @throws IOException
     *             the io exception
     * @since 1.59
     */
    public void add(GHUser u) throws IOException {
        root().createRequest().method("PUT").withUrlPath(api("/memberships/" + u.getLogin())).send();
    }

    /**
     * Adds a member to the team
     * <p>
     * The user will be invited to the organization if required.
     *
     * @param user
     *            github user
     * @param role
     *            role for the new member
     * @throws IOException
     *             the io exception
     */
    public void add(GHUser user, Role role) throws IOException {
        root().createRequest()
                .method("PUT")
                .with("role", role)
                .withUrlPath(api("/memberships/" + user.getLogin()))
                .send();
    }

    /**
     * Connect an external group to the team
     *
     * @param group
     *            the group to connect
     * @return the external group
     * @throws IOException
     *             in case of failure
     * @see <a href=
     *      "https://docs.github.com/en/enterprise-cloud@latest/rest/teams/external-groups?apiVersion=2022-11-28#update-the-connection-between-an-external-group-and-a-team">documentation</a>
     */
    public GHExternalGroup connectToExternalGroup(final GHExternalGroup group) throws IOException {
        return connectToExternalGroup(group.getId());
    }

    /**
     * Connect an external group to the team
     *
     * @param group_id
     *            the identifier of the group to connect
     * @return the external group
     * @throws IOException
     *             in case of failure
     * @see <a href=
     *      "https://docs.github.com/en/enterprise-cloud@latest/rest/teams/external-groups?apiVersion=2022-11-28#update-the-connection-between-an-external-group-and-a-team">documentation</a>
     */
    public GHExternalGroup connectToExternalGroup(final long group_id) throws IOException {
        try {
            return root().createRequest()
                    .method("PATCH")
                    .with("group_id", group_id)
                    .withUrlPath(publicApi(EXTERNAL_GROUPS))
                    .fetch(GHExternalGroup.class)
                    .wrapUp(getOrganization());
        } catch (final HttpException e) {
            throw EnterpriseManagedSupport.forOrganization(getOrganization())
                    .filterException(e, "Could not connect team to external group")
                    .orElse(e);
        }
    }

    /**
     * Begins the creation of a new instance.
     *
     * Consumer must call {@link GHDiscussion.Creator#done()} to commit changes.
     *
     * @param title
     *            title of the discussion to be created
     * @return a {@link GHDiscussion.Creator}
     * @throws IOException
     *             the io exception
     */
    public GHDiscussion.Creator createDiscussion(String title) throws IOException {
        return GHDiscussion.create(this).title(title);
    }

    /**
     * Deletes this team.
     *
     * @throws IOException
     *             the io exception
     */
    public void delete() throws IOException {
        root().createRequest().method("DELETE").withUrlPath(api("")).send();
    }

    /**
     * Remove the connection of the team to an external group
     *
     * @throws IOException
     *             in case of failure
     * @see <a href=
     *      "https://docs.github.com/en/enterprise-cloud@latest/rest/teams/external-groups?apiVersion=2022-11-28#remove-the-connection-between-an-external-group-and-a-team">documentation</a>
     */
    public void deleteExternalGroupConnection() throws IOException {
        root().createRequest().method("DELETE").withUrlPath(publicApi(EXTERNAL_GROUPS)).send();
    }

    /**
     * Equals.
     *
     * @param o
     *            the o
     * @return true, if successful
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GHTeam ghTeam = (GHTeam) o;
        return Objects.equals(name, ghTeam.name) && Objects.equals(getUrl(), ghTeam.getUrl())
                && Objects.equals(permission, ghTeam.permission) && Objects.equals(slug, ghTeam.slug)
                && Objects.equals(description, ghTeam.description) && Objects.equals(privacy, ghTeam.privacy);
    }

    /**
     * Gets description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets a single discussion by ID.
     *
     * @param discussionNumber
     *            id of the discussion that we want to query for
     * @return the discussion
     * @throws IOException
     *             the io exception
     * @see <a href= "https://developer.github.com/v3/teams/discussions/#get-a-discussion">documentation</a>
     */
    @Nonnull
    public GHDiscussion getDiscussion(long discussionNumber) throws IOException {
        return GHDiscussion.read(this, discussionNumber);
    }

    /**
     * Get the external groups connected to the team
     *
     * @return the external groups
     * @throws IOException
     *             the io exception
     * @see <a href=
     *      "https://docs.github.com/en/enterprise-cloud@latest/rest/teams/external-groups?apiVersion=2022-11-28#list-a-connection-between-an-external-group-and-a-team">documentation</a>
     */
    public List<GHExternalGroup> getExternalGroups() throws IOException {
        try {
            return Collections.unmodifiableList(Arrays.asList(root().createRequest()
                    .method("GET")
                    .withUrlPath(publicApi(EXTERNAL_GROUPS))
                    .fetch(GHExternalGroupPage.class)
                    .getGroups()));
        } catch (final HttpException e) {
            throw EnterpriseManagedSupport.forOrganization(getOrganization())
                    .filterException(e, "Could not retrieve team external groups")
                    .orElse(e);
        }
    }

    /**
     * Gets the html url.
     *
     * @return the html url
     */
    public URL getHtmlUrl() {
        return GitHubClient.parseURL(htmlUrl);
    }

    /**
     * Gets members.
     *
     * @return the members
     * @throws IOException
     *             the io exception
     */
    public Set<GHUser> getMembers() throws IOException {
        return listMembers().toSet();
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
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
        refresh(organization);
        return organization;
    }

    /**
     * Gets permission.
     *
     * @return the permission
     */
    public String getPermission() {
        return permission;
    }

    /**
     * Gets the privacy state.
     *
     * @return the privacy state.
     */
    public Privacy getPrivacy() {
        return EnumUtils.getNullableEnumOrDefault(Privacy.class, privacy, Privacy.UNKNOWN);
    }

    /**
     * Gets repositories.
     *
     * @return the repositories
     */
    public Map<String, GHRepository> getRepositories() {
        Map<String, GHRepository> m = new TreeMap<>();
        for (GHRepository r : listRepositories()) {
            m.put(r.getName(), r);
        }
        return m;
    }

    /**
     * Gets slug.
     *
     * @return the slug
     */
    public String getSlug() {
        return slug;
    }

    /**
     * Checks if this team has the specified user as a member.
     *
     * @param user
     *            the user
     * @return the boolean
     */
    public boolean hasMember(GHUser user) {
        try {
            root().createRequest().withUrlPath(api("/memberships/" + user.getLogin())).send();
            return true;
        } catch (@SuppressWarnings("unused") IOException ignore) {
            return false;
        }
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, getUrl(), permission, slug, description, privacy);
    }

    /**
     * Retrieves the teams that are children of this team.
     *
     * @return the paged iterable
     */
    public PagedIterable<GHTeam> listChildTeams() {
        return root().createRequest()
                .withUrlPath(api("/teams"))
                .toIterable(GHTeam[].class, item -> item.wrapUp(this.organization));
    }

    /**
     * Retrieves the discussions.
     *
     * @return the paged iterable
     */
    @Nonnull
    public PagedIterable<GHDiscussion> listDiscussions() {
        return GHDiscussion.readAll(this);
    }

    /**
     * Retrieves the current members.
     *
     * @return the paged iterable
     */
    public PagedIterable<GHUser> listMembers() {
        return listMembers("all");
    }

    /**
     * List members with specified role paged iterable.
     *
     * @param role
     *            the role
     * @return the paged iterable
     */
    public PagedIterable<GHUser> listMembers(Role role) {
        return listMembers(transformEnum(role));
    }

    /**
     * List members with specified role paged iterable.
     *
     * @param role
     *            the role
     * @return the paged iterable
     */
    public PagedIterable<GHUser> listMembers(String role) {
        return root().createRequest().withUrlPath(api("/members")).with("role", role).toIterable(GHUser[].class, null);
    }

    /**
     * List repositories paged iterable.
     *
     * @return the paged iterable
     */
    public PagedIterable<GHRepository> listRepositories() {
        return root().createRequest().withUrlPath(api("/repos")).toIterable(GHRepository[].class, null);
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

    /**
     * Remove.
     *
     * @param r
     *            the r
     * @throws IOException
     *             the io exception
     */
    public void remove(GHRepository r) throws IOException {
        root().createRequest()
                .method("DELETE")
                .withUrlPath(api("/repos/" + r.getOwnerName() + '/' + r.getName()))
                .send();
    }

    /**
     * Removes a member to the team.
     *
     * @param u
     *            the u
     * @throws IOException
     *             the io exception
     */
    public void remove(GHUser u) throws IOException {
        root().createRequest().method("DELETE").withUrlPath(api("/memberships/" + u.getLogin())).send();
    }

    /**
     * Sets description.
     *
     * @param description
     *            the description
     * @throws IOException
     *             the io exception
     */
    public void setDescription(String description) throws IOException {
        root().createRequest().method("PATCH").with("description", description).withUrlPath(api("")).send();
    }

    /**
     * Updates the team's privacy setting.
     *
     * @param privacy
     *            the privacy
     * @throws IOException
     *             the io exception
     */
    public void setPrivacy(Privacy privacy) throws IOException {
        root().createRequest().method("PATCH").with("privacy", privacy).withUrlPath(api("")).send();
    }

    private String api(String tail) {
        if (organization == null) {
            // Teams returned from pull requests to do not have an organization. Attempt to use url.
            final URL url = Objects.requireNonNull(getUrl(), "Missing instance URL!");
            return StringUtils.prependIfMissing(url.toString().replace(root().getApiUrl(), ""), "/") + tail;
        }

        return "/organizations/" + organization.getId() + "/team/" + getId() + tail;
    }

    private String publicApi(String tail) throws IOException {
        return "/orgs/" + getOrganization().login + "/teams/" + getSlug() + tail;
    }

    /**
     * Wrap up.
     *
     * @param owner
     *            the owner
     * @return the GH team
     */
    GHTeam wrapUp(GHOrganization owner) {
        this.organization = owner;
        return this;
    }

    /**
     * Wrap up.
     *
     * @param root
     *            the root
     * @return the GH team
     */
    GHTeam wrapUp(GitHub root) { // auto-wrapUp when organization is known from GET /user/teams
        return wrapUp(organization);
    }

}
