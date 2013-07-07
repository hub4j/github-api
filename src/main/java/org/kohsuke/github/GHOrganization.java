package org.kohsuke.github;

import java.io.IOException;
import java.util.AbstractList;
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
        return createRepository(name,description,homepage,getTeams().get(team),isPublic);
    }

    public GHRepository createRepository(String name, String description, String homepage, GHTeam team, boolean isPublic) throws IOException {
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
     * All the members of this organization.
     */
    public List<GHUser> getMembers() throws IOException {
        return new AbstractList<GHUser>() {
            // these are shallow objects with only some limited values filled out
            // TODO: it's better to allow objects to fill themselves in later when missing values are requested
            final GHUser[] shallow = root.retrieve().to("/orgs/" + login + "/members", GHUser[].class);

            @Override
            public GHUser get(int index) {
                try {
                    return root.getUser(shallow[index].getLogin());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public int size() {
                return shallow.length;
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
}
