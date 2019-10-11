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
 * @author Kohsuke Kawaguchi
 */
public class GHOrganization extends GHPerson {
    /*package*/ GHOrganization wrapUp(GitHub root) {
        return (GHOrganization)super.wrapUp(root);
    }

    /**
     * Creates a new repository.
     *
     * @return
     *      Newly created repository.
     * @deprecated
     *      Use {@link #createRepository(String)} that uses a builder pattern to let you control every aspect.
     */
    public GHRepository createRepository(String name, String description, String homepage, String team, boolean isPublic) throws IOException {
        GHTeam t = getTeams().get(team);
        if (t==null)
            throw new IllegalArgumentException("No such team: "+team);
        return createRepository(name, description, homepage, t, isPublic);
    }

    /**
     * @deprecated
     *      Use {@link #createRepository(String)} that uses a builder pattern to let you control every aspect.
     */
    public GHRepository createRepository(String name, String description, String homepage, GHTeam team, boolean isPublic) throws IOException {
        if (team==null)
            throw new IllegalArgumentException("Invalid team");
        return createRepository(name).description(description).homepage(homepage).private_(!isPublic).team(team).create();
    }

    /**
     * Starts a builder that creates a new repository.
     *
     * <p>
     * You use the returned builder to set various properties, then call {@link GHCreateRepositoryBuilder#create()}
     * to finally createa repository.
     */
    public GHCreateRepositoryBuilder createRepository(String name) {
        return new GHCreateRepositoryBuilder(root,"/orgs/"+login+"/repos",name);
    }

    /**
     * Teams by their names.
     */
    public Map<String,GHTeam> getTeams() throws IOException {
        Map<String,GHTeam> r = new TreeMap<String, GHTeam>();
        for (GHTeam t : listTeams()) {
            r.put(t.getName(),t);
        }
        return r;
    }

    /**
     * List up all the teams.
     */
    public PagedIterable<GHTeam> listTeams() throws IOException {
        return new PagedIterable<GHTeam>() {
            public PagedIterator<GHTeam> _iterator(int pageSize) {
                return new PagedIterator<GHTeam>(root.retrieve().asIterator(String.format("/orgs/%s/teams", login), GHTeam[].class, pageSize)) {
                    @Override
                    protected void wrapUp(GHTeam[] page) {
                        GHTeam.wrapUp(page, GHOrganization.this);
                    }
                };
            }
        };
    }

    /**
     * Finds a team that has the given name in its {@link GHTeam#getName()}
     */
    public GHTeam getTeamByName(String name) throws IOException {
        for (GHTeam t : listTeams()) {
            if(t.getName().equals(name))
                return t;
        }
        return null;
    }

    /**
     * Finds a team that has the given slug in its {@link GHTeam#getSlug()}
     */
    public GHTeam getTeamBySlug(String slug) throws IOException {
        for (GHTeam t : listTeams()) {
            if(t.getSlug().equals(slug))
                return t;
        }
        return null;
    }

    /** Member's role in an organization */
    public enum Role {
        ADMIN, /** The user is an owner of the organization. */
        MEMBER /** The user is a non-owner member of the organization. */
    }

    /**
     * Adds (invites) a user to the organization.
     * @see <a href="https://developer.github.com/v3/orgs/members/#add-or-update-organization-membership">documentation</a>
     */
    public void add(GHUser user, Role role) throws IOException {
        root.retrieve().method("PUT")
                .with("role", role.name().toLowerCase())
                .to("/orgs/" + login + "/memberships/" + user.getLogin());
    }

    /**
     * Checks if this organization has the specified user as a member.
     */
    public boolean hasMember(GHUser user) {
        try {
            root.retrieve().to("/orgs/" + login + "/members/"  + user.getLogin());
            return true;
        } catch (IOException ignore) {
            return false;
        }
    }

    /**
     * Remove a member of the organisation - which will remove them from
     * all teams, and remove their access to the organization’s repositories.
     */
    public void remove(GHUser user) throws IOException {
        root.retrieve().method("DELETE").to("/orgs/" + login + "/members/"  + user.getLogin());
    }

    /**
     * Checks if this organization has the specified user as a public member.
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
     */
    public void publicize(GHUser u) throws IOException {
        root.retrieve().method("PUT").to("/orgs/" + login + "/public_members/" + u.getLogin(), null);
    }

    /**
     * @deprecated use {@link #listMembers()}
     */
    public List<GHUser> getMembers() throws IOException {
        return listMembers().asList();
    }

    /**
     * All the members of this organization.
     */
    public PagedIterable<GHUser> listMembers() throws IOException {
        return listMembers("members");
    }

    /**
     * All the public members of this organization.
     */
    public PagedIterable<GHUser> listPublicMembers() throws IOException {
        return listMembers("public_members");
    }

    private PagedIterable<GHUser> listMembers(String suffix) throws IOException {
        return listMembers(suffix, null);
    }

    public PagedIterable<GHUser> listMembersWithFilter(String filter) throws IOException {
        return listMembers("members", filter);
    }

    private PagedIterable<GHUser> listMembers(final String suffix, final String filter) throws IOException {
        return new PagedIterable<GHUser>() {
            public PagedIterator<GHUser> _iterator(int pageSize) {
                String filterParams = (filter == null) ? "" : ("?filter=" + filter);
                return new PagedIterator<GHUser>(root.retrieve().asIterator(String.format("/orgs/%s/%s%s", login, suffix, filterParams), GHUser[].class, pageSize)) {
                    @Override
                    protected void wrapUp(GHUser[] users) {
                        GHUser.wrap(users, root);
                    }
                };
            }
        };
    }

    /**
     * Conceals the membership.
     */
    public void conceal(GHUser u) throws IOException {
        root.retrieve().method("DELETE").to("/orgs/" + login + "/public_members/" + u.getLogin(), null);
    }

    /**
     * Returns the projects for this organization.
     * @param status The status filter (all, open or closed).
     */
    public PagedIterable<GHProject> listProjects(final GHProject.ProjectStateFilter status) throws IOException {
        return new PagedIterable<GHProject>() {
            public PagedIterator<GHProject> _iterator(int pageSize) {
                return new PagedIterator<GHProject>(root.retrieve().withPreview(INERTIA)
                        .with("state", status)
                        .asIterator(String.format("/orgs/%s/projects", login), GHProject[].class, pageSize)) {
                    @Override
                    protected void wrapUp(GHProject[] page) {
                        for (GHProject c : page)
                            c.wrap(root);
                    }
                };
            }
        };
    }

    /**
     * Returns all open projects for the organization.
     */
    public PagedIterable<GHProject> listProjects() throws IOException {
        return listProjects(GHProject.ProjectStateFilter.OPEN);
    }

    /**
     * Creates a project for the organization.
     */
    public GHProject createProject(String name, String body) throws IOException {
        return root.retrieve().method("POST")
                .withPreview(INERTIA)
                .with("name", name)
                .with("body", body)
                .to(String.format("/orgs/%s/projects", login), GHProject.class).wrap(root);
    }

    public enum Permission { ADMIN, PUSH, PULL }

    /**
     * Creates a new team and assigns the repositories.
     */
    public GHTeam createTeam(String name, Permission p, Collection<GHRepository> repositories) throws IOException {
        Requester post = new Requester(root).with("name", name).with("permission", p);
        List<String> repo_names = new ArrayList<String>();
        for (GHRepository r : repositories) {
            repo_names.add(r.getName());
        }
        post.with("repo_names",repo_names);
        return post.method("POST").to("/orgs/" + login + "/teams", GHTeam.class).wrapUp(this);
    }

    public GHTeam createTeam(String name, Permission p, GHRepository... repositories) throws IOException {
        return createTeam(name, p, Arrays.asList(repositories));
    }

    /**
     * List up repositories that has some open pull requests.
     *
     * This used to be an efficient method that didn't involve traversing every repository, but now
     * it doesn't do any optimization.
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
        return new PagedIterable<GHEventInfo>() {
            public PagedIterator<GHEventInfo> _iterator(int pageSize) {
                return new PagedIterator<GHEventInfo>(root.retrieve().asIterator(String.format("/orgs/%s/events", login), GHEventInfo[].class, pageSize)) {
                    @Override
                    protected void wrapUp(GHEventInfo[] page) {
                        for (GHEventInfo c : page)
                            c.wrapUp(root);
                    }
                };
            }
        };
    }

    /**
     * Lists up all the repositories using the specified page size.
     *
     * @param pageSize size for each page of items returned by GitHub. Maximum page size is 100.
     *
     * Unlike {@link #getRepositories()}, this does not wait until all the repositories are returned.
     */
    @Override
    public PagedIterable<GHRepository> listRepositories(final int pageSize) {
        return new PagedIterable<GHRepository>() {
            public PagedIterator<GHRepository> _iterator(int pageSize) {
                return new PagedIterator<GHRepository>(root.retrieve().asIterator("/orgs/" + login + "/repos", GHRepository[].class, pageSize)) {
                    @Override
                    protected void wrapUp(GHRepository[] page) {
                        for (GHRepository c : page)
                            c.wrap(root);
                    }
                };
            }
        }.withPageSize(pageSize);
    }

    /**
     * Retrieves the currently configured hooks.
     */
    public List<GHHook> getHooks() throws IOException {
        return GHHooks.orgContext(this).getHooks();
    }

    public GHHook getHook(int id) throws IOException {
        return GHHooks.orgContext(this).getHook(id);
    }

    /**
     *
     * See https://api.github.com/hooks for possible names and their configuration scheme.
     * TODO: produce type-safe binding
     *
     * @param name
     *      Type of the hook to be created. See https://api.github.com/hooks for possible names.
     * @param config
     *      The configuration hash.
     * @param events
     *      Can be null. Types of events to hook into.
     */
    public GHHook createHook(String name, Map<String,String> config, Collection<GHEvent> events, boolean active) throws IOException {
        return GHHooks.orgContext(this).createHook(name, config, events, active);
    }

    public GHHook createWebHook(URL url, Collection<GHEvent> events) throws IOException {
        return createHook("web", Collections.singletonMap("url", url.toExternalForm()),events,true);
    }

    public GHHook createWebHook(URL url) throws IOException {
        return createWebHook(url, null);
    }
}
