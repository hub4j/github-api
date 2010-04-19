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
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.kohsuke.github.GitHub.MAPPER;

/**
 * Represents an user of GitHub.
 *
 * @author Kohsuke Kawaguchi
 */
public class GHUser {
    /*package almost final*/ GitHub root;

    private String gravatar_id,name,company,location,created_at,blog,login,email;
    private int public_gist_count,public_repo_count,following_count,id,followers_count;

    /**
     * Repositories that this user owns.
     */
    private transient Map<String,GHRepository> repositories;

    /**
     * Gravatar ID of this user, like 0cb9832a01c22c083390f3c5dcb64105
     */
    public String getGravatarId() {
        return gravatar_id;
    }

    /**
     * Gets the human-readable name of the user, like "Kohsuke Kawaguchi"
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the company name of this user, like "Sun Microsystems, Inc."
     */
    public String getCompany() {
        return company;
    }

    /**
     * Gets the location of this user, like "Santa Clara, California"
     */
    public String getLocation() {
        return location;
    }

    public String getCreatedAt() {
        return created_at;
    }

    /**
     * Gets the blog URL of this user.
     */
    public String getBlog() {
        return blog;
    }

    /**
     * Gets the login ID of this user, like 'kohsuke'
     */
    public String getLogin() {
        return login;
    }

    /**
     * Gets the e-mail address of the user.
     */
    public String getEmail() {
        return email;
    }

    public int getPublicGistCount() {
        return public_gist_count;
    }

    public int getPublicRepoCount() {
        return public_repo_count;
    }

    public int getFollowingCount() {
        return following_count;
    }

    /**
     * What appears to be a GitHub internal unique number that identifies this user.
     */
    public int getId() {
        return id;
    }

    public int getFollowersCount() {
        return followers_count;
    }

    /**
     * Gets the repositories this user owns.
     */
    public Map<String,GHRepository> getRepositories() throws IOException {
        if (repositories==null) {
            repositories = new TreeMap<String, GHRepository>();
            URL url = new URL("http://github.com/api/v2/json/repos/show/" + login);
            for (GHRepository r : MAPPER.readValue(url, JsonRepositories.class).repositories) {
                r.root = root;
                repositories.put(r.getName(),r);
            }
        }
        
        return Collections.unmodifiableMap(repositories);
    }

    public GHRepository getRepository(String name) throws IOException {
        return getRepositories().get(name);
    }

    /**
     * Follow this user.
     */
    public void follow() throws IOException {
        new Poster(root).to(root.getApiURL("/user/follow/"+login));
    }

    /**
     * Unfollow this user.
     */
    public void unfollow() throws IOException {
        new Poster(root).to(root.getApiURL("/user/unfollow/"+login));
    }

    /**
     * Lists the users that this user is following
     */
    public Set<GHUser> getFollows() throws IOException {
        return root.retrieve("/user/show/"+login+"/followers",JsonUsers.class).toSet(root);
    }

    /**
     * Lists the users who are following this user.
     */
    public Set<GHUser> getFollowings() throws IOException {
        return root.retrieve("/user/show/"+login+"/following",JsonUsers.class).toSet(root);
    }

    @Override
    public String toString() {
        return "User:"+login;
    }

    @Override
    public int hashCode() {
        return login.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GHUser) {
            GHUser that = (GHUser) obj;
            return this.login.equals(that.login);
        }
        return false;
    }
}
