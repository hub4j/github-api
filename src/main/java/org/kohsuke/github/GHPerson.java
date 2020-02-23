package org.kohsuke.github;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Common part of {@link GHUser} and {@link GHOrganization}.
 */
public abstract class GHPerson extends GHObject {
    /* package almost final */ GitHub root;

    // core data fields that exist even for "small" user data (such as the user info in pull request)
    protected String login, avatar_url, gravatar_id;

    // other fields (that only show up in full data)
    protected String location, blog, email, name, company, type;
    protected String html_url;
    protected int followers, following, public_repos, public_gists;
    protected boolean site_admin;

    // other fields (that only show up in full data) that require privileged scope
    protected Integer total_private_repos;

    GHPerson wrapUp(GitHub root) {
        this.root = root;
        return this;
    }

    /**
     * Fully populate the data by retrieving missing data.
     * <p>
     * Depending on the original API call where this object is created, it may not contain everything.
     *
     * @throws IOException
     *             the io exception
     */
    protected synchronized void populate() throws IOException {
        if (created_at != null) {
            return; // already populated
        }
        if (root == null || root.isOffline()) {
            return; // cannot populate, will have to live with what we have
        }
        root.createRequest().withUrlPath(url).fetchInto(this);
    }

    /**
     * Gets the public repositories this user owns.
     *
     * <p>
     * To list your own repositories, including private repositories, use {@link GHMyself#listRepositories()}
     *
     * @return the repositories
     * @throws IOException
     *             the io exception
     */
    public synchronized Map<String, GHRepository> getRepositories() throws IOException {
        Map<String, GHRepository> repositories = new TreeMap<String, GHRepository>();
        for (GHRepository r : listRepositories(100)) {
            repositories.put(r.getName(), r);
        }
        return Collections.unmodifiableMap(repositories);
    }

    /**
     * Lists up all the repositories using a 30 items page size.
     * <p>
     * Unlike {@link #getRepositories()}, this does not wait until all the repositories are returned.
     *
     * @return the paged iterable
     */
    public PagedIterable<GHRepository> listRepositories() {
        return listRepositories(30);
    }

    /**
     * Lists up all the repositories using the specified page size.
     *
     * @param pageSize
     *            size for each page of items returned by GitHub. Maximum page size is 100. Unlike
     *            {@link #getRepositories()}, this does not wait until all the repositories are returned.
     * @return the paged iterable
     */
    public PagedIterable<GHRepository> listRepositories(final int pageSize) {
        return root.createRequest()
                .withUrlPath("/users/" + login + "/repos")
                .toIterable(GHRepository[].class, item -> item.wrap(root))
                .withPageSize(pageSize);
    }

    /**
     * Loads repository list in a paginated fashion.
     *
     * <p>
     * For a person with a lot of repositories, GitHub returns the list of repositories in a paginated fashion. Unlike
     * {@link #getRepositories()}, this method allows the caller to start processing data as it arrives.
     * <p>
     * Every {@link Iterator#next()} call results in I/O. Exceptions that occur during the processing is wrapped into
     * {@link Error}.
     *
     * @param pageSize
     *            the page size
     * @return the iterable
     * @deprecated Use {@link #listRepositories()}
     */
    @Deprecated
    public synchronized Iterable<List<GHRepository>> iterateRepositories(final int pageSize) {
        return new Iterable<List<GHRepository>>() {
            public Iterator<List<GHRepository>> iterator() {
                final Iterator<GHRepository[]> pager = GitHubPageIterator.create(root.getClient(),
                        GHRepository[].class,
                        root.createRequest().withUrlPath("users", login, "repos").withPageSize(pageSize));

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
     * Gets repository.
     *
     * @param name
     *            the name
     * @return null if the repository was not found
     * @throws IOException
     *             the io exception
     */
    public GHRepository getRepository(String name) throws IOException {
        try {
            return root.createRequest()
                    .withUrlPath("/repos/" + login + '/' + name)
                    .fetch(GHRepository.class)
                    .wrap(root);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    /**
     * Lists events for an organization or an user.
     *
     * @return the paged iterable
     * @throws IOException
     *             the io exception
     */
    public abstract PagedIterable<GHEventInfo> listEvents() throws IOException;

    /**
     * Gravatar ID of this user, like 0cb9832a01c22c083390f3c5dcb64105
     *
     * @return the gravatar id
     * @deprecated No longer available in the v3 API.
     */
    public String getGravatarId() {
        return gravatar_id;
    }

    /**
     * Returns a string like 'https://secure.gravatar.com/avatar/0cb9832a01c22c083390f3c5dcb64105' that indicates the
     * avatar image URL.
     *
     * @return the avatar url
     */
    public String getAvatarUrl() {
        if (avatar_url != null)
            return avatar_url;
        if (gravatar_id != null)
            return "https://secure.gravatar.com/avatar/" + gravatar_id;
        return null;
    }

    /**
     * Gets the login ID of this user, like 'kohsuke'
     *
     * @return the login
     */
    public String getLogin() {
        return login;
    }

    /**
     * Gets the human-readable name of the user, like "Kohsuke Kawaguchi"
     *
     * @return the name
     * @throws IOException
     *             the io exception
     */
    public String getName() throws IOException {
        populate();
        return name;
    }

    /**
     * Gets the company name of this user, like "Sun Microsystems, Inc."
     *
     * @return the company
     * @throws IOException
     *             the io exception
     */
    public String getCompany() throws IOException {
        populate();
        return company;
    }

    /**
     * Gets the location of this user, like "Santa Clara, California"
     *
     * @return the location
     * @throws IOException
     *             the io exception
     */
    public String getLocation() throws IOException {
        populate();
        return location;
    }

    public Date getCreatedAt() throws IOException {
        populate();
        return super.getCreatedAt();
    }

    public Date getUpdatedAt() throws IOException {
        populate();
        return super.getUpdatedAt();
    }

    /**
     * Gets the blog URL of this user.
     *
     * @return the blog
     * @throws IOException
     *             the io exception
     */
    public String getBlog() throws IOException {
        populate();
        return blog;
    }

    @Override
    public URL getHtmlUrl() {
        return GitHubClient.parseURL(html_url);
    }

    /**
     * Gets the e-mail address of the user.
     *
     * @return the email
     * @throws IOException
     *             the io exception
     */
    public String getEmail() throws IOException {
        populate();
        return email;
    }

    /**
     * Gets public gist count.
     *
     * @return the public gist count
     * @throws IOException
     *             the io exception
     */
    public int getPublicGistCount() throws IOException {
        populate();
        return public_gists;
    }

    /**
     * Gets public repo count.
     *
     * @return the public repo count
     * @throws IOException
     *             the io exception
     */
    public int getPublicRepoCount() throws IOException {
        populate();
        return public_repos;
    }

    /**
     * Gets following count.
     *
     * @return the following count
     * @throws IOException
     *             the io exception
     */
    public int getFollowingCount() throws IOException {
        populate();
        return following;
    }

    /**
     * Gets followers count.
     *
     * @return the followers count
     * @throws IOException
     *             the io exception
     */
    public int getFollowersCount() throws IOException {
        populate();
        return followers;
    }

    /**
     * Gets the type. This is either "User" or "Organization".
     *
     * @return the type
     * @throws IOException
     *             the io exception
     */
    public String getType() throws IOException {
        populate();
        return type;
    }

    /**
     * Gets the site_admin field
     *
     * @return the site_admin field
     * @throws IOException
     *             the io exception
     */
    public boolean isSiteAdmin() throws IOException {
        populate();
        return site_admin;
    }

    /**
     * Gets total private repo count.
     *
     * @return the total private repo count
     * @throws IOException
     *             the io exception
     */
    public Optional<Integer> getTotalPrivateRepoCount() throws IOException {
        populate();
        return Optional.ofNullable(total_private_repos);
    }
}
