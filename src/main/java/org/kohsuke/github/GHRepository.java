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

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;
import java.net.URL;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.*;

/**
 * A repository on GitHub.
 * 
 * @author Kohsuke Kawaguchi
 */
public class GHRepository {
    /*package almost final*/ GitHub root;

    private String description, homepage, url, name, owner;
    private boolean has_issues, has_wiki, fork, _private, has_downloads;
    private int watchers,forks;

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
            new Poster(root).withCredential().to(root.getApiURL("/repos/collaborators/"+name+ op +user.getLogin()));
        }
    }

    /**
     * Deletes this repository.
     */
    public void delete() throws IOException {
        verifyMine();
        Poster poster = new Poster(root).withCredential();
        URL url = root.getApiURL("/repos/delete/" + name);

        DeleteToken token = poster.to(url, DeleteToken.class);
        poster.with("delete_token",token.delete_token).to(url);
    }

    /**
     * Forks this repository.
     */
    public GHRepository fork() throws IOException {
        return new Poster(root).withCredential().to(root.getApiURL("/repos/fork/" + owner + "/" + name), JsonRepository.class).wrap(root);
    }

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
}
