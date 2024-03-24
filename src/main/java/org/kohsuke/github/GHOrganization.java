package org.kohsuke.github;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

// TODO: Auto-generated Javadoc
/**
 * The type GHOrganization.
 *
 * @author Kohsuke Kawaguchi
 */
public class GHOrganization extends GHPerson {

    private boolean has_organization_projects;

    /**
     * Starts a builder that creates a new repository.
     * <p>
     * You use the returned builder to set various properties, then call {@link GHCreateRepositoryBuilder#create()} to
     * finally create a repository.
     *
     * @param name
     *            the name
     * @return the gh create repository builder
     */
    public GHCreateRepositoryBuilder createRepository(String name) {
        return new GHCreateRepositoryBuilder(name, root(), "/orgs/" + login + "/repos");
    }

    /**
     * Teams by their names.
     *
     * @return the teams
     * @throws IOException
     *             the io exception
     */
    public Map<String, GHTeam> getTeams() throws IOException {
        Map<String, GHTeam> r = new TreeMap<String, GHTeam>();
        for (GHTeam t : listTeams()) {
            r.put(t.getName(), t);
        }
        return r;
    }

    /**
     * List up all the teams.
     *
     * @return the paged iterable
     * @throws IOException
     *             the io exception
     */
    public PagedIterable<GHTeam> listTeams() throws IOException {
        return root().createRequest()
                .withUrlPath(String.format("/orgs/%s/teams", login))
                .toIterable(GHTeam[].class, item -> item.wrapUp(this));
    }

    /**
     * Gets a single team by ID.
     *
     * @param teamId
     *            id of the team that we want to query for
     * @return the team
     * @throws IOException
     *             the io exception
     * @see <a href= "https://developer.github.com/v3/teams/#get-team-by-name">documentation</a>
     */
    public GHTeam getTeam(long teamId) throws IOException {
        return root().createRequest()
                .withUrlPath(String.format("/organizations/%d/team/%d", getId(), teamId))
                .fetch(GHTeam.class)
                .wrapUp(this);
    }

    /**
     * Finds a team that has the given name in its {@link GHTeam#getName()}.
     *
     * @param name
     *            the name
     * @return the team by name
     * @throws IOException
     *             the io exception
     */
    public GHTeam getTeamByName(String name) throws IOException {
        for (GHTeam t : listTeams()) {
            if (t.getName().equals(name))
                return t;
        }
        return null;
    }

    /**
     * Finds a team that has the given slug in its {@link GHTeam#getSlug()}.
     *
     * @param slug
     *            the slug
     * @return the team by slug
     * @throws IOException
     *             the io exception
     * @see <a href= "https://developer.github.com/v3/teams/#get-team-by-name">documentation</a>
     */
    public GHTeam getTeamBySlug(String slug) throws IOException {
        return root().createRequest()
                .withUrlPath(String.format("/orgs/%s/teams/%s", login, slug))
                .fetch(GHTeam.class)
                .wrapUp(this);
    }

    /**
     * Member's role in an organization.
     */
    public enum Role {

        /** The admin. */
        ADMIN,
        /** The user is an owner of the organization. */
        MEMBER /** The user is a non-owner member of the organization. */
    }

    /**
     * Adds (invites) a user to the organization.
     *
     * @param user
     *            the user
     * @param role
     *            the role
     * @throws IOException
     *             the io exception
     * @see <a href=
     *      "https://developer.github.com/v3/orgs/members/#add-or-update-organization-membership">documentation</a>
     */
    public void add(GHUser user, Role role) throws IOException {
        root().createRequest()
                .method("PUT")
                .with("role", role.name().toLowerCase())
                .withUrlPath("/orgs/" + login + "/memberships/" + user.getLogin())
                .send();
    }

    /**
     * Checks if this organization has the specified user as a member.
     *
     * @param user
     *            the user
     * @return the boolean
     */
    public boolean hasMember(GHUser user) {
        try {
            root().createRequest().withUrlPath("/orgs/" + login + "/members/" + user.getLogin()).send();
            return true;
        } catch (IOException ignore) {
            return false;
        }
    }

    /**
     * Remove a member of the organisation - which will remove them from all teams, and remove their access to the
     * organization’s repositories.
     *
     * @param user
     *            the user
     * @throws IOException
     *             the io exception
     */
    public void remove(GHUser user) throws IOException {
        root().createRequest().method("DELETE").withUrlPath("/orgs/" + login + "/members/" + user.getLogin()).send();
    }

    /**
     * Checks if this organization has the specified user as a public member.
     *
     * @param user
     *            the user
     * @return the boolean
     */
    public boolean hasPublicMember(GHUser user) {
        try {
            root().createRequest().withUrlPath("/orgs/" + login + "/public_members/" + user.getLogin()).send();
            return true;
        } catch (IOException ignore) {
            return false;
        }
    }

    /**
     * Publicizes the membership.
     *
     * @param u
     *            the u
     * @throws IOException
     *             the io exception
     */
    public void publicize(GHUser u) throws IOException {
        root().createRequest().method("PUT").withUrlPath("/orgs/" + login + "/public_members/" + u.getLogin()).send();
    }

    /**
     * All the members of this organization.
     *
     * @return the paged iterable
     * @throws IOException
     *             the io exception
     */
    public PagedIterable<GHUser> listMembers() throws IOException {
        return listMembers("members");
    }

    /**
     * All the public members of this organization.
     *
     * @return the paged iterable
     * @throws IOException
     *             the io exception
     */
    public PagedIterable<GHUser> listPublicMembers() throws IOException {
        return listMembers("public_members");
    }

    /**
     * All the outside collaborators of this organization.
     *
     * @return the paged iterable
     * @throws IOException
     *             the io exception
     */
    public PagedIterable<GHUser> listOutsideCollaborators() throws IOException {
        return listMembers("outside_collaborators");
    }

    private PagedIterable<GHUser> listMembers(String suffix) throws IOException {
        return listMembers(suffix, null, null);
    }

    /**
     * List members with filter paged iterable.
     *
     * @param filter
     *            the filter
     * @return the paged iterable
     * @throws IOException
     *             the io exception
     */
    public PagedIterable<GHUser> listMembersWithFilter(String filter) throws IOException {
        return listMembers("members", filter, null);
    }

    /**
     * List outside collaborators with filter paged iterable.
     *
     * @param filter
     *            the filter
     * @return the paged iterable
     * @throws IOException
     *             the io exception
     */
    public PagedIterable<GHUser> listOutsideCollaboratorsWithFilter(String filter) throws IOException {
        return listMembers("outside_collaborators", filter, null);
    }

    /**
     * List members with specified role paged iterable.
     *
     * @param role
     *            the role
     * @return the paged iterable
     * @throws IOException
     *             the io exception
     */
    public PagedIterable<GHUser> listMembersWithRole(String role) throws IOException {
        return listMembers("members", null, role);
    }

    private PagedIterable<GHUser> listMembers(final String suffix, final String filter, String role)
            throws IOException {
        return root().createRequest()
                .withUrlPath(String.format("/orgs/%s/%s", login, suffix))
                .with("filter", filter)
                .with("role", role)
                .toIterable(GHUser[].class, null);
    }

    /**
     * Conceals the membership.
     *
     * @param u
     *            the u
     * @throws IOException
     *             the io exception
     */
    public void conceal(GHUser u) throws IOException {
        root().createRequest()
                .method("DELETE")
                .withUrlPath("/orgs/" + login + "/public_members/" + u.getLogin())
                .send();
    }

    /**
     * Are projects enabled for organization boolean.
     *
     * @return the boolean
     */
    public boolean areOrganizationProjectsEnabled() {
        return has_organization_projects;
    }

    /**
     * Sets organization projects enabled status boolean.
     *
     * @param newStatus
     *            enable status
     * @throws IOException
     *             the io exception
     */
    public void enableOrganizationProjects(boolean newStatus) throws IOException {
        edit("has_organization_projects", newStatus);
    }

    private void edit(String key, Object value) throws IOException {
        root().createRequest()
                .withUrlPath(String.format("/orgs/%s", login))
                .method("PATCH")
                .with(key, value)
                .fetchInto(this);
    }

    /**
     * Returns the projects for this organization.
     *
     * @param status
     *            The status filter (all, open or closed).
     * @return the paged iterable
     * @throws IOException
     *             the io exception
     */
    public PagedIterable<GHProject> listProjects(final GHProject.ProjectStateFilter status) throws IOException {
        return root().createRequest()
                .with("state", status)
                .withUrlPath(String.format("/orgs/%s/projects", login))
                .toIterable(GHProject[].class, null);
    }

    /**
     * Returns all open projects for the organization.
     *
     * @return the paged iterable
     * @throws IOException
     *             the io exception
     */
    public PagedIterable<GHProject> listProjects() throws IOException {
        return listProjects(GHProject.ProjectStateFilter.OPEN);
    }

    /**
     * Creates a project for the organization.
     *
     * @param name
     *            the name
     * @param body
     *            the body
     * @return the gh project
     * @throws IOException
     *             the io exception
     */
    public GHProject createProject(String name, String body) throws IOException {
        return root().createRequest()
                .method("POST")
                .with("name", name)
                .with("body", body)
                .withUrlPath(String.format("/orgs/%s/projects", login))
                .fetch(GHProject.class);
    }

    /**
     * The enum Permission.
     *
     * @see RepositoryRole
     */
    public enum Permission {

        /** The admin. */
        ADMIN,
        /** The maintain. */
        MAINTAIN,
        /** The push. */
        PUSH,
        /** The triage. */
        TRIAGE,
        /** The pull. */
        PULL,
        /** Unknown, before we add the new permission to the enum */
        UNKNOWN
    }

    /**
     * Repository permissions (roles) for teams and collaborators.
     */
    public static class RepositoryRole {
        private final String permission;

        private RepositoryRole(String permission) {
            this.permission = permission;
        }

        /**
         * Custom.
         *
         * @param permission
         *            the permission
         * @return the repository role
         */
        public static RepositoryRole custom(String permission) {
            return new RepositoryRole(permission);
        }

        /**
         * From.
         *
         * @param permission
         *            the permission
         * @return the repository role
         */
        public static RepositoryRole from(Permission permission) {
            return custom(permission.toString().toLowerCase());
        }

        /**
         * To string.
         *
         * @return the string
         */
        @Override
        public String toString() {
            return permission;
        }
    }

    /**
     * Starts a builder that creates a new team.
     * <p>
     * You use the returned builder to set various properties, then call {@link GHTeamBuilder#create()} to finally
     * create a team.
     *
     * @param name
     *            the name
     * @return the gh create repository builder
     */
    public GHTeamBuilder createTeam(String name) {
        return new GHTeamBuilder(root(), login, name);
    }

    /**
     * List up repositories that has some open pull requests.
     * <p>
     * This used to be an efficient method that didn't involve traversing every repository, but now it doesn't do any
     * optimization.
     *
     * @return the repositories with open pull requests
     * @throws IOException
     *             the io exception
     */
    public List<GHRepository> getRepositoriesWithOpenPullRequests() throws IOException {
        List<GHRepository> r = new ArrayList<GHRepository>();
        for (GHRepository repository : listRepositories().withPageSize(100)) {
            List<GHPullRequest> pullRequests = repository.queryPullRequests().state(GHIssueState.OPEN).list().toList();
            if (pullRequests.size() > 0) {
                r.add(repository);
            }
        }
        return r;
    }

    /**
     * Gets all the open pull requests in this organization.
     *
     * @return the pull requests
     * @throws IOException
     *             the io exception
     */
    public List<GHPullRequest> getPullRequests() throws IOException {
        List<GHPullRequest> all = new ArrayList<GHPullRequest>();
        for (GHRepository r : getRepositoriesWithOpenPullRequests()) {
            all.addAll(r.queryPullRequests().state(GHIssueState.OPEN).list().toList());
        }
        return all;
    }

    /**
     * Lists events performed by a user (this includes private events if the caller is authenticated.
     *
     * @return the paged iterable
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public PagedIterable<GHEventInfo> listEvents() throws IOException {
        return root().createRequest()
                .withUrlPath(String.format("/orgs/%s/events", login))
                .toIterable(GHEventInfo[].class, null);
    }

    /**
     * Lists up all the repositories using the specified page size.
     *
     * @return the paged iterable
     */
    @Override
    public PagedIterable<GHRepository> listRepositories() {
        return root().createRequest()
                .withUrlPath("/orgs/" + login + "/repos")
                .toIterable(GHRepository[].class, null)
                .withPageSize(30);
    }

    /**
     * Retrieves the currently configured hooks.
     *
     * @return the hooks
     * @throws IOException
     *             the io exception
     */
    public List<GHHook> getHooks() throws IOException {
        return GHHooks.orgContext(this).getHooks();
    }

    /**
     * Gets hook.
     *
     * @param id
     *            the id
     * @return the hook
     * @throws IOException
     *             the io exception
     */
    public GHHook getHook(int id) throws IOException {
        return GHHooks.orgContext(this).getHook(id);
    }

    /**
     * Deletes hook.
     *
     * @param id
     *            the id
     * @throws IOException
     *             the io exception
     */
    public void deleteHook(int id) throws IOException {
        GHHooks.orgContext(this).deleteHook(id);
    }

    /**
     * See https://api.github.com/hooks for possible names and their configuration scheme. TODO: produce type-safe
     * binding
     *
     * @param name
     *            Type of the hook to be created. See https://api.github.com/hooks for possible names.
     * @param config
     *            The configuration hash.
     * @param events
     *            Can be null. Types of events to hook into.
     * @param active
     *            the active
     * @return the gh hook
     * @throws IOException
     *             the io exception
     */
    public GHHook createHook(String name, Map<String, String> config, Collection<GHEvent> events, boolean active)
            throws IOException {
        return GHHooks.orgContext(this).createHook(name, config, events, active);
    }

    /**
     * Create web hook gh hook.
     *
     * @param url
     *            the url
     * @param events
     *            the events
     * @return the gh hook
     * @throws IOException
     *             the io exception
     */
    public GHHook createWebHook(URL url, Collection<GHEvent> events) throws IOException {
        return createHook("web", Collections.singletonMap("url", url.toExternalForm()), events, true);
    }

    /**
     * Create web hook gh hook.
     *
     * @param url
     *            the url
     * @return the gh hook
     * @throws IOException
     *             the io exception
     */
    public GHHook createWebHook(URL url) throws IOException {
        return createWebHook(url, null);
    }
}
