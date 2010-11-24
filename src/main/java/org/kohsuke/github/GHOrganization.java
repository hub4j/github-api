package org.kohsuke.github;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;
import java.util.Map;

/**
 * @author Kohsuke Kawaguchi
 */
public class GHOrganization extends GHPerson {
    /**
     * Creates a new repository.
     *
     * @return
     *      Newly created repository.
     */
    public GHRepository createRepository(String name, String description, String homepage, String team, boolean isPublic) throws IOException {
        // such API doesn't exist, so fall back to HTML scraping
        WebClient wc = root.createWebClient();
        HtmlPage pg = (HtmlPage)wc.getPage("https://github.com/organizations/"+login+"/repositories/new");
        HtmlForm f = pg.getForms().get(1);
        f.getInputByName("repository[name]").setValueAttribute(name);
        f.getInputByName("repository[description]").setValueAttribute(description);
        f.getInputByName("repository[homepage]").setValueAttribute(homepage);
        f.getSelectByName("team_id").getOptionByText(team).setSelected(true);
        f.submit(f.getButtonByCaption("Create Repository"));

        return root.getUser(login).getRepository(name);

//        GHRepository r = new Poster(root).withCredential()
//                .with("name", name).with("description", description).with("homepage", homepage)
//                .with("public", isPublic ? 1 : 0).to(root.getApiURL("/organizations/"+login+"/repos/create"), JsonRepository.class).repository;
//        r.root = root;
//        return r;
    }

    /**
     * Teams by their names.
     */
    public Map<String,GHTeam> getTeams() throws IOException {
        return root.retrieveWithAuth(root.getApiURL("/organizations/"+login+"/teams"),JsonTeams.class).toMap(this);
    }
}
