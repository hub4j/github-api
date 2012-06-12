package org.kohsuke.github;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

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
        return new Poster(root).withCredential()
                .with("name", name).with("description", description).with("homepage", homepage)
                .with("public", isPublic).with("team_id",team.getId()).to("/orgs/"+login+"/repos", GHRepository.class).wrap(root);
    }

    /**
     * Teams by their names.
     */
    public Map<String,GHTeam> getTeams() throws IOException {
        GHTeam[] teams = root.retrieveWithAuth3("/orgs/" + login + "/teams", GHTeam[].class);
        Map<String,GHTeam> r = new TreeMap<String, GHTeam>();
        for (GHTeam t : teams) {
            r.put(t.getName(),t);
        }
        return r;
    }

    /**
     * Publicizes the membership.
     */
    public void publicize(GHUser u) throws IOException {
        root.retrieveWithAuth3("/orgs/" + login + "/public_members/" + u.getLogin(), null, "PUT");
    }

    /**
     * All the members of this organization.
     */
    public List<GHUser> getMembers() throws IOException {
        return new AbstractList<GHUser>() {
            // these are shallow objects with only some limited values filled out
            // TODO: it's better to allow objects to fill themselves in later when missing values are requested
            final GHUser[] shallow = root.retrieveWithAuth3("/orgs/" + login + "/members", GHUser[].class);

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
        root.retrieveWithAuth3("/orgs/" + login + "/public_members/" + u.getLogin(), null, "DELETE");
    }

    public enum Permission { ADMIN, PUSH, PULL }

    /**
     * Creates a new team and assigns the repositories.
     */
    public GHTeam createTeam(String name, Permission p, Collection<GHRepository> repositories) throws IOException {
        Poster post = new Poster(root).withCredential().with("name", name).with("permission", p.name().toLowerCase());
        List<String> repo_names = new ArrayList<String>();
        for (GHRepository r : repositories) {
            repo_names.add(r.getName());
        }
        post.with("repo_names",repo_names);
        return post.to("/orgs/"+login+"/teams",GHTeam.class,"POST").wrapUp(this);
    }

    public GHTeam createTeam(String name, Permission p, GHRepository... repositories) throws IOException {
        return createTeam(name,p, Arrays.asList(repositories));
    }

    /**
     * List up repositories that has some open pull requests.
     */
    public List<GHRepository> getRepositoriesWithOpenPullRequests() throws IOException {
        WebClient wc = root.createWebClient();
        HtmlPage pg = (HtmlPage)wc.getPage("https://github.com/organizations/"+login+"/dashboard/pulls");
        List<GHRepository> r = new ArrayList<GHRepository>();
        for (HtmlAnchor e : pg.getElementById("js-issue-list").<HtmlAnchor>selectNodes(".//UL[@class='smallnav']/LI[not(@class='zeroed')]/A")) {
            String a = e.getHrefAttribute();
            String name = a.substring(a.lastIndexOf('/')+1);
            r.add(getRepository(name));
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
