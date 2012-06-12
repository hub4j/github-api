package org.kohsuke.github;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Common part of {@link GHUser} and {@link GHOrganization}.
 * 
 * @author Kohsuke Kawaguchi
 */
public abstract class GHPerson {
    /*package almost final*/ GitHub root;

    // common
    protected String login,location,blog,email,name,created_at,company;
    protected int id;
    protected String gravatar_id; // appears in V3 as well but presumably subsumed by avatar_url?

    // V2
    protected int public_gist_count,public_repo_count,followers_count,following_count;

    // V3
    protected String avatar_url,html_url;
    protected int followers,following,public_repos,public_gists;

    /*package*/ GHPerson wrapUp(GitHub root) {
        this.root = root;
        return this;
    }

    /**
     * Gets the repositories this user owns.
     */
    public synchronized Map<String,GHRepository> getRepositories() throws IOException {
        Map<String,GHRepository> repositories = new TreeMap<String, GHRepository>();
        for (List<GHRepository> batch : iterateRepositories(100)) {
            for (GHRepository r : batch)
                repositories.put(r.getName(),r);
        }
        return Collections.unmodifiableMap(repositories);
    }

    /**
     * Loads repository list in a pagenated fashion.
     * 
     * <p>
     * For a person with a lot of repositories, GitHub returns the list of repositories in a pagenated fashion.
     * Unlike {@link #getRepositories()}, this method allows the caller to start processing data as it arrives.
     * 
     * Every {@link Iterator#next()} call results in I/O. Exceptions that occur during the processing is wrapped
     * into {@link Error}.
     */
    public synchronized Iterable<List<GHRepository>> iterateRepositories(final int pageSize) {
        return new Iterable<List<GHRepository>>() {
            public Iterator<List<GHRepository>> iterator() {
                final Iterator<GHRepository[]> pager = root.retrievePaged("/users/" + login + "/repos?per_page="+pageSize,GHRepository[].class,false);

                return new Iterator<List<GHRepository>>() {
                    public boolean hasNext() {
                        return pager.hasNext();
                    }

                    public List<GHRepository> next() {
                        GHRepository[] batch = pager.next();
                        for (GHRepository r : batch)
                            r.root = root;
                        return Arrays.asList(batch);
                    }

                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    /**
     *
     * @return
     *      null if the repository was not found
     */
    public GHRepository getRepository(String name) throws IOException {
        try {
            return root.retrieveWithAuth("/repos/" + login + '/' + name, GHRepository.class).wrap(root);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    /**
     * Gravatar ID of this user, like 0cb9832a01c22c083390f3c5dcb64105
     *
     * @deprecated
     *      No longer available in the v3 API.
     */
    public String getGravatarId() {
        return gravatar_id;
    }

    /**
     * Returns a string like 'https://secure.gravatar.com/avatar/0cb9832a01c22c083390f3c5dcb64105'
     * that indicates the avatar image URL.
     */
    public String getAvatarUrl() {
        if (avatar_url!=null)
            return avatar_url;
        if (gravatar_id!=null)
            return "https://secure.gravatar.com/avatar/"+gravatar_id;
        return null;
    }

    /**
     * Gets the login ID of this user, like 'kohsuke'
     */
    public String getLogin() {
        return login;
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
     * Gets the e-mail address of the user.
     */
    public String getEmail() {
        return email;
    }

    public int getPublicGistCount() {
        return Math.max(public_gist_count,public_gists);
    }

    public int getPublicRepoCount() {
        return Math.max(public_repo_count,public_repos);
    }

    public int getFollowingCount() {
        return Math.max(following_count,following);
    }

    /**
     * What appears to be a GitHub internal unique number that identifies this user.
     */
    public int getId() {
        return id;
    }

    public int getFollowersCount() {
        return Math.max(followers_count,followers);
    }

}
