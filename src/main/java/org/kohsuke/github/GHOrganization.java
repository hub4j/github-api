package org.kohsuke.github;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
     */
    public GHRepository createRepository(String name, String description, String homepage, String team, boolean isPublic) throws IOException {
        GHTeam t = getTeams().get(team);
        if (t==null)
            throw new IllegalArgumentException("No such team: "+team);
        return createRepository(name,description,homepage,t,isPublic);
    }

    public GHRepository createRepository(String name, String description, String homepage, GHTeam team, boolean isPublic) throws IOException {
        if (team==null)
            throw new IllegalArgumentException("Invalid team");
        // such API doesn't exist, so fall back to HTML scraping
        return new Requester(root)
                .with("name", name).with("description", description).with("homepage", homepage)
                .with("public", isPublic).with("team_id",team.getId()).to("/orgs/"+login+"/repos", GHRepository.class).wrap(root);
    }

    /**
     * Teams by their names.
     */
    public Map<String,GHTeam> getTeams() throws IOException {
        GHTeam[] teams = root.retrieve().to("/orgs/" + login + "/teams", GHTeam[].class);
        Map<String,GHTeam> r = new TreeMap<String, GHTeam>();
        for (GHTeam t : teams) {
            r.put(t.getName(),t.wrapUp(this));
        }
        return r;
    }

    /**
     * List up all the teams.
     */
    public PagedIterable<GHTeam> listTeams() throws IOException {
        return new PagedIterable<GHTeam>() {
            public PagedIterator<GHTeam> iterator() {
                return new PagedIterator<GHTeam>(root.retrieve().asIterator(String.format("/orgs/%s/teams", login), GHTeam[].class)) {
                    @Override
                    protected void wrapUp(GHTeam[] page) {
                        for (GHTeam c : page)
                            c.wrapUp(GHOrganization.this);
                    }
                };
            }
        };
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
            public PagedIterator<GHUser> iterator() {
                String filterParams = (filter == null) ? "" : ("?filter=" + filter);
                return new PagedIterator<GHUser>(root.retrieve().asIterator(String.format("/orgs/%s/%s%s", login, suffix, filterParams), GHUser[].class)) {
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

    public enum Permission { ADMIN, PUSH, PULL }

    /**
     * Creates a new team and assigns the repositories.
     */
    public GHTeam createTeam(String name, Permission p, Collection<GHRepository> repositories) throws IOException {
        Requester post = new Requester(root).with("name", name).with("permission", p.name().toLowerCase());
        List<String> repo_names = new ArrayList<String>();
        for (GHRepository r : repositories) {
            repo_names.add(r.getName());
        }
        post.with("repo_names",repo_names);
        return post.method("POST").to("/orgs/" + login + "/teams", GHTeam.class).wrapUp(this);
    }

    public GHTeam createTeam(String name, Permission p, GHRepository... repositories) throws IOException {
        return createTeam(name,p, Arrays.asList(repositories));
    }

    /**
     * List up repositories that has some open pull requests.
     *
     * This used to be an efficient method that didn't involve traversing every repository, but now
     * it doesn't do any optimization.
     */
    public List<GHRepository> getRepositoriesWithOpenPullRequests() throws IOException {
        List<GHRepository> r = new ArrayList<GHRepository>();
        for (GHRepository repository : listRepositories()) {
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
            public PagedIterator<GHEventInfo> iterator() {
                return new PagedIterator<GHEventInfo>(root.retrieve().asIterator(String.format("/orgs/%s/events", login), GHEventInfo[].class)) {
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
            public PagedIterator<GHRepository> iterator() {
                return new PagedIterator<GHRepository>(root.retrieve().asIterator("/orgs/" + login + "/repos?per_page=" + pageSize, GHRepository[].class)) {
                    @Override
                    protected void wrapUp(GHRepository[] page) {
                        for (GHRepository c : page)
                            c.wrap(root);
                    }
                };
            }
        };
    }
}
