/*
 * The MIT License
 *
 * Copyright (c) 2010, Kohsuke Kawaguchi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.kohsuke.github;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static java.util.Arrays.*;
import static org.kohsuke.github.ApiVersion.V3;

/**
 * A repository on GitHub.
 * 
 * @author Kohsuke Kawaguchi
 */
@SuppressWarnings({"UnusedDeclaration"})
public class GHRepository {
    /*package almost final*/ GitHub root;

    private String description, homepage, url, name, owner;
    private boolean has_issues, has_wiki, fork, _private, has_downloads;
    private int watchers,forks,open_issues;
    private String created_at, pushed_at;

    public String getDescription() {
        return description;
    }

    public String getHomepage() {
        return homepage;
    }

    /**
     * URL of this repository, like 'http://github.com/kohsuke/hudson'
     */
    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public GHUser getOwner() throws IOException {
        return root.getUser(owner);
    }

    public List<GHIssue> getIssues(GHIssueState state) throws IOException {
       return root.retrieve("/issues/list/" + owner + "/" + name + "/" + state.toString().toLowerCase(), JsonIssues.class).wrap(this);
    }

    protected String getOwnerName() {
        return owner;
    }

    public boolean hasIssues() {
        return has_issues;
    }

    public boolean hasWiki() {
        return has_wiki;
    }

    public boolean isFork() {
        return fork;
    }

    public int getForks() {
        return forks;
    }

    public boolean isPrivate() {
        return _private;
    }

    public boolean hasDownloads() {
        return has_downloads;
    }

    public int getWatchers() {
        return watchers;
    }

    public int getOpenIssueCount() {
        return open_issues;
    }

    public Date getPushedAt() {
        return GitHub.parseDate(pushed_at);
    }

    public Date getCreatedAt() {
        return GitHub.parseDate(created_at);
    }


    /**
     * Gets the collaborators on this repository.
     * This set always appear to include the owner.
     */
    public Set<GHUser> getCollaborators() throws IOException {
        Set<GHUser> r = new HashSet<GHUser>();
        for (String u : root.retrieve("/repos/show/"+owner+"/"+name+"/collaborators",JsonCollaborators.class).collaborators)
            r.add(root.getUser(u));
        return Collections.unmodifiableSet(r);
    }

    /**
     * Gets the names of the collaborators on this repository.
     * This method deviates from the principle of this library but it works a lot faster than {@link #getCollaborators()}.
     */
    public Set<String> getCollaboratorNames() throws IOException {
        Set<String> r = new HashSet<String>(root.retrieve("/repos/show/"+owner+"/"+name+"/collaborators",JsonCollaborators.class).collaborators);
        return Collections.unmodifiableSet(r);
    }

    /**
     * If this repository belongs to an organization, return a set of teams.
     */
    public Set<GHTeam> getTeams() throws IOException {
        return Collections.unmodifiableSet(root.retrieveWithAuth("/repos/show/"+owner+"/"+name+"/teams",JsonTeams.class).toSet(
                root.getOrganization(owner)));
    }

    public void addCollaborators(GHUser... users) throws IOException {
        addCollaborators(asList(users));
    }

    public void addCollaborators(Collection<GHUser> users) throws IOException {
        modifyCollaborators(users, "/add/");
    }

    public void removeCollaborators(GHUser... users) throws IOException {
        removeCollaborators(asList(users));
    }

    public void removeCollaborators(Collection<GHUser> users) throws IOException {
        modifyCollaborators(users, "/remove/");
    }

    private void modifyCollaborators(Collection<GHUser> users, String op) throws IOException {
        verifyMine();
        for (GHUser user : users) {
            new Poster(root).withCredential().to("/repos/collaborators/"+name+ op +user.getLogin());
        }
    }

    public void setEmailServiceHook(String address) throws IOException {
        WebClient wc = root.createWebClient();
        HtmlPage pg = (HtmlPage)wc.getPage(getUrl()+"/admin");
        HtmlInput email = (HtmlInput)pg.getElementById("Email_address");
        email.setValueAttribute(address);
        HtmlCheckBoxInput active = (HtmlCheckBoxInput)pg.getElementById("Email[active]");
        active.setChecked(true);

        final HtmlForm f = email.getEnclosingFormOrDie();
        f.submit((HtmlButton) f.getElementsByTagName("button").get(0));
    }

    /**
     * Enables or disables the issue tracker for this repository.
     */
    public void enableIssueTracker(boolean v) throws IOException {
        new Poster(root).withCredential().with("values[has_issues]",String.valueOf(v))
                .to("/repos/show/" + owner + "/" + name);
    }

    /**
     * Enables or disables Wiki for this repository.
     */
    public void enableWiki(boolean v) throws IOException {
        new Poster(root).withCredential().with("values[has_wiki]",String.valueOf(v))
                .to("/repos/show/" + owner + "/" + name);
    }

    /**
     * Deletes this repository.
     */
    public void delete() throws IOException {
        Poster poster = new Poster(root).withCredential();
        String url = "/repos/delete/" + owner +"/"+name;

        DeleteToken token = poster.to(url, DeleteToken.class);
        poster.with("delete_token",token.delete_token).to(url);
    }

    /**
     * Forks this repository as your repository.
     *
     * @return
     *      Newly forked repository that belong to you.
     */
    public GHRepository fork() throws IOException {
        return new Poster(root).withCredential().to("/repos/fork/" + owner + "/" + name, JsonRepository.class).wrap(root);
    }

    /**
     * Forks this repository into an organization.
     *
     * @return
     *      Newly forked repository that belong to you.
     */
    public GHRepository forkTo(GHOrganization org) throws IOException {
        new Poster(root, V3).withCredential().to(String.format("/repos/%s/%s/forks?org=%s",owner,name,org.getLogin()));
        return org.getRepository(name);
    }

    /**
     * Rename this repository.
     */
    public void renameTo(String newName) throws IOException {
        WebClient wc = root.createWebClient();
        HtmlPage pg = (HtmlPage)wc.getPage(getUrl()+"/admin");
        for (HtmlForm f : pg.getForms()) {
            if (!f.getActionAttribute().endsWith("/rename"))  continue;
            try {
                f.getInputByName("name").setValueAttribute(newName);
                f.submit((HtmlButton)f.getElementsByTagName("button").get(0));

                // overwrite fields
                final GHRepository r = getOwner().getRepository(newName);
                for (Field fi : getClass().getDeclaredFields()) {
                    if (Modifier.isStatic(fi.getModifiers()))   continue;
                    fi.setAccessible(true);
                    try {
                        fi.set(this,fi.get(r));
                    } catch (IllegalAccessException e) {
                        throw (IllegalAccessError)new IllegalAccessError().initCause(e);
                    }
                }

                return;
            } catch (ElementNotFoundException e) {
                // continue
            }
        }

        throw new IllegalArgumentException("Either you don't have the privilege to rename "+owner+'/'+name+" or there's a bug in HTML scraping");
    }

    /**
     * Retrieves a specified pull request.
     */
    public GHPullRequest getPullRequest(int i) throws IOException {
        return root.retrieveWithAuth("/pulls/" + owner + '/' + name + "/" + i, JsonPullRequest.class).wrap(this);
    }

    /**
     * Retrieves all the pull requests of a particular state.
     */
    public List<GHPullRequest> getPullRequests(GHIssueState state) throws IOException {
        return root.retrieveWithAuth("/pulls/"+owner+'/'+name+"/"+state.name().toLowerCase(Locale.ENGLISH),JsonPullRequests.class).wrap(this);
    }

// this is no different from getPullRequests(OPEN)
//    /**
//     * Retrieves all the pull requests.
//     */
//    public List<GHPullRequest> getPullRequests() throws IOException {
//        return root.retrieveWithAuth("/pulls/"+owner+'/'+name,JsonPullRequests.class).wrap(root);
//    }

    private void verifyMine() throws IOException {
        if (!root.login.equals(owner))
            throw new IOException("Operation not applicable to a repository owned by someone else: "+owner);
    }

    /**
     * Returns a set that represents the post-commit hook URLs.
     * The returned set is live, and changes made to them are reflected to GitHub.
     */
    public Set<URL> getPostCommitHooks() {
        return postCommitHooks;
    }

    /**
     * Live set view of the post-commit hook.
     */
    private final Set<URL> postCommitHooks = new AbstractSet<URL>() {
        private List<URL> getPostCommitHooks() {
            try {
                verifyMine();

                HtmlForm f = getForm();

                List<URL> r = new ArrayList<URL>();
                for (HtmlInput i : f.getInputsByName("urls[]")) {
                    String v = i.getValueAttribute();
                    if (v.length()==0)  continue;
                    r.add(new URL(v));
                }
                return r;
            } catch (IOException e) {
                throw new GHException("Failed to retrieve post-commit hooks",e);
            }
        }

        @Override
        public Iterator<URL> iterator() {
            return getPostCommitHooks().iterator();
        }

        @Override
        public int size() {
            return getPostCommitHooks().size();
        }

        @Override
        public boolean add(URL url) {
            try {
                String u = url.toExternalForm();

                verifyMine();

                HtmlForm f = getForm();

                List<HtmlInput> controls = f.getInputsByName("urls[]");
                for (HtmlInput i : controls) {
                    String v = i.getValueAttribute();
                    if (v.length()==0)  continue;
                    if (v.equals(u))
                        return false;   // already there
                }

                controls.get(controls.size()-1).setValueAttribute(u);
                f.submit(null);
                return true;
            } catch (IOException e) {
                throw new GHException("Failed to update post-commit hooks",e);
            }
        }

        @Override
        public boolean remove(Object o) {
            try {
                String u = ((URL)o).toExternalForm();

                verifyMine();

                HtmlForm f = getForm();

                List<HtmlInput> controls = f.getInputsByName("urls[]");
                for (HtmlInput i : controls) {
                    String v = i.getValueAttribute();
                    if (v.length()==0)  continue;
                    if (v.equals(u)) {
                        i.setValueAttribute("");
                        f.submit(null);
                        return true;
                    }
                }

                return false;
            } catch (IOException e) {
                throw new GHException("Failed to update post-commit hooks",e);
            }
        }

        private HtmlForm getForm() throws IOException {
            WebClient wc = root.createWebClient();
            HtmlPage pg = (HtmlPage)wc.getPage(getUrl()+"/admin");
            HtmlForm f = (HtmlForm) pg.getElementById("new_service");
            return f;
        }
    };


    @Override
    public String toString() {
        return "Repository:"+owner+":"+name;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GHRepository) {
            GHRepository that = (GHRepository) obj;
            return this.owner.equals(that.owner)
                && this.name.equals(that.name);
        }
        return false;
    }
}
