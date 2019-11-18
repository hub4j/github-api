package org.kohsuke.github;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import static org.kohsuke.github.Previews.INERTIA;

/**
 * The type GHOrganization.
 *
 * @author Kohsuke Kawaguchi
 */
public class GHOrganization extends GHPerson {
    GHOrganization wrapUp(GitHub root) {
        return (GHOrganization) super.wrapUp(root);
    }

    /**
     * Creates a new repository.
     *
     * @param name
     *            the name
     * @param description
     *            the description
     * @param homepage
     *            the homepage
     * @param team
     *            the team
     * @param isPublic
     *            the is public
     * @return Newly created repository.
     * @throws IOException
     *             the io exception
     * @deprecated Use {@link #createRepository(String)} that uses a builder pattern to let you control every aspect.
     */
    public GHRepository createRepository(String name,
            String description,
            String homepage,
            String team,
            boolean isPublic) throws IOException {
        GHTeam t = getTeams().get(team);
        if (t == null)
            throw new IllegalArgumentException("No such team: " + team);
        return createRepository(name, description, homepage, t, isPublic);
    }

    /**
     * Create repository gh repository.
     *
     * @param name
     *            the name
     * @param description
     *            the description
     * @param homepage
     *            the homepage
     * @param team
     *            the team
     * @param isPublic
     *            the is public
     * @return the gh repository
     * @throws IOException
     *             the io exception
     * @deprecated Use {@link #createRepository(String)} that uses a builder pattern to let you control every aspect.
     */
    public GHRepository createRepository(String name,
            String description,
            String homepage,
            GHTeam team,
            boolean isPublic) throws IOException {
        if (team == null)
            throw new IllegalArgumentException("Invalid team");
        return createRepository(name).description(description)
                .homepage(homepage)
                .private_(!isPublic)
                .team(team)
                .create();
    }

    /**
     * Starts a builder that creates a new repository.
     *
     * <p>
     * You use the returned builder to set various properties, then call {@link GHCreateRepositoryBuilder#create()} to
     * finally createa repository.
     *
     * @param name
     *            the name
     * @return the gh create repository builder
     */
    public GHCreateRepositoryBuilder createRepository(String name) {
        return new GHCreateRepositoryBuilder(root, "/orgs/" + login + "/repos", name);
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
        return root.retrieve()
                .asPagedIterable(String.format("/orgs/%s/teams", login),
                        GHTeam[].class,
                        item -> item.wrapUp(GHOrganization.this));
    }

    /**
     * Finds a team that has the given name in its {@link GHTeam#getName()}
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
     * Finds a team that has the given slug in its {@link GHTeam#getSlug()}
     *
     * @param slug
     *            the slug
     * @return the team by slug
     * @throws IOException
     *             the io exception
     */
    public GHTeam getTeamBySlug(String slug) throws IOException {
        for (GHTeam t : listTeams()) {
            if (t.getSlug().equals(slug))
                return t;
        }
        return null;
    }

    /**
     * Member's role in an organization
     */
    public enum Role {
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
        root.retrieve()
                .method("PUT")
                .with("role", role.name().toLowerCase())
                .to("/orgs/" + login + "/memberships/" + user.getLogin());
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
            root.retrieve().to("/orgs/" + login + "/members/" + user.getLogin());
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
        root.retrieve().method("DELETE").to("/orgs/" + login + "/members/" + user.getLogin());
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
            root.retrieve().to("/orgs/" + login + "/public_members/" + user.getLogin());
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
        root.retrieve().method("PUT").to("/orgs/" + login + "/public_members/" + u.getLogin(), null);
    }

    /**
     * Gets members.
     *
     * @return the members
     * @throws IOException
     *             the io exception
     * @deprecated use {@link #listMembers()}
     */
    public List<GHUser> getMembers() throws IOException {
        return listMembers().asList();
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

    private PagedIterable<GHUser> listMembers(String suffix) throws IOException {
        return listMembers(suffix, null);
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
        return listMembers("members", filter);
    }

    private PagedIterable<GHUser> listMembers(final String suffix, final String filter) throws IOException {
        String filterParams = (filter == null) ? "" : ("?filter=" + filter);
        return root.retrieve()
                .asPagedIterable(String.format("/orgs/%s/%s%s", login, suffix, filterParams),
                        GHUser[].class,
                        item -> item.wrapUp(root));
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
        root.retrieve().method("DELETE").to("/orgs/" + login + "/public_members/" + u.getLogin(), null);
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
        return root.retrieve()
                .withPreview(INERTIA)
                .with("state", status)
                .asPagedIterable(String.format("/orgs/%s/projects", login), GHProject[].class, item -> item.wrap(root));
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
        return root.retrieve()
                .method("POST")
                .withPreview(INERTIA)
                .with("name", name)
                .with("body", body)
                .to(String.format("/orgs/%s/projects", login), GHProject.class)
                .wrap(root);
    }

    /**
     * The enum Permission.
     */
    public enum Permission {
        ADMIN, PUSH, PULL
    }

    /**
     * Creates a new team and assigns the repositories.
     *
     * @param name
     *            the name
     * @param repositories
     *            the repositories
     * @return the gh team
     * @throws IOException
     *             the io exception
     */
    public GHTeam createTeam(String name, Collection<GHRepository> repositories) throws IOException {
        Requester post = new Requester(root).with("name", name);
        List<String> repo_names = new ArrayList<String>();
        for (GHRepository r : repositories) {
            repo_names.add(login + "/" + r.getName());
        }
        post.with("repo_names", repo_names);
        return post.method("POST").to("/orgs/" + login + "/teams", GHTeam.class).wrapUp(this);
    }

    /**
     * Create team gh team.
     *
     * @param name
     *            the name
     * @param repositories
     *            the repositories
     * @return the gh team
     * @throws IOException
     *             the io exception
     */
    public GHTeam createTeam(String name, GHRepository... repositories) throws IOException {
        return createTeam(name, Arrays.asList(repositories));
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
        for (GHRepository repository : listRepositories(100)) {
            repository.wrap(root);
            List<GHPullRequest> pullRequests = repository.getPullRequests(GHIssueState.OPEN);
            if (pullRequests.size() > 0) {
                r.add(repository);
            }
        }
        return r;
    }

    /**
     * Gets all the open pull requests in this organizataion.
     *
     * @return the pull requests
     * @throws IOException
     *             the io exception
     */
    public List<GHPullRequest> getPullRequests() throws IOException {
        List<GHPullRequest> all = new ArrayList<GHPullRequest>();
        for (GHRepository r : getRepositoriesWithOpenPullRequests()) {
            all.addAll(r.getPullRequests(GHIssueState.OPEN));
        }
        return all;
    }

    /**
     * Lists events performed by a user (this includes private events if the caller is authenticated.
     */
    public PagedIterable<GHEventInfo> listEvents() throws IOException {
        return root.retrieve()
                .asPagedIterable(String.format("/orgs/%s/events", login),
                        GHEventInfo[].class,
                        item -> item.wrapUp(root));
    }

    /**
     * Lists up all the repositories using the specified page size.
     *
     * @param pageSize
     *            size for each page of items returned by GitHub. Maximum page size is 100.
     *
     *            Unlike {@link #getRepositories()}, this does not wait until all the repositories are returned.
     */
    @Override
    public PagedIterable<GHRepository> listRepositories(final int pageSize) {
        return root.retrieve()
                .asPagedIterable("/orgs/" + login + "/repos", GHRepository[].class, item -> item.wrap(root))
                .withPageSize(pageSize);
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
