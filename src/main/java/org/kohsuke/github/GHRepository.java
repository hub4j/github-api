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

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

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

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public GHUser getOwner() throws IOException {
        return root.getUser(owner);
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
        GHRepository r = new Poster(root).withCredential().to(root.getApiURL("/repos/fork/" + owner + "/" + name), JsonRepository.class).repository;
        r.root = root;
        return r;
    }

    private void verifyMine() throws IOException {
        if (!root.login.equals(owner))
            throw new IOException("Operation not applicable to a repository owned by someone else: "+owner);
    }

    @Override
    public String toString() {
        return "Repository:"+owner+":"+name;
    }
}
