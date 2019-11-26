package org.kohsuke.github;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Represents the account that's logging into GitHub.
 *
 * @author Kohsuke Kawaguchi
 */
public class GHMyself extends GHUser {

    /**
     * Type of repositories returned during listing.
     */
    public enum RepositoryListFilter {
        /**
         * All public and private repositories that current user has access or collaborates to
         */
        ALL,
        /**
         * Public and private repositories owned by current user
         */
        OWNER,
        /**
         * Public repositories that current user has access or collaborates to
         */
        PUBLIC,
        /**
         * Private repositories that current user has access or collaborates to
         */
        PRIVATE,
        /**
         * Public and private repositories that current user is a member
         */
        MEMBER;
    }

    /**
     * Gets emails.
     *
     * @return the emails
     * @throws IOException
     *             the io exception
     * @deprecated Use {@link #getEmails2()}
     */
    public List<String> getEmails() throws IOException {
        List<GHEmail> src = getEmails2();
        List<String> r = new ArrayList<String>(src.size());
        for (GHEmail e : src) {
            r.add(e.getEmail());
        }
        return r;
    }

    /**
     * Returns the read-only list of e-mail addresses configured for you.
     * <p>
     * This corresponds to the stuff you configure in https://github.com/settings/emails, and not to be confused with
     * {@link #getEmail()} that shows your public e-mail address set in https://github.com/settings/profile
     *
     * @return Always non-null.
     * @throws IOException
     *             the io exception
     */
    public List<GHEmail> getEmails2() throws IOException {
        GHEmail[] addresses = root.retrieve().withUrlPath("/user/emails").toArray(GHEmail[].class);
        return Collections.unmodifiableList(Arrays.asList(addresses));
    }

    /**
     * Returns the read-only list of all the pulic keys of the current user.
     * <p>
     * NOTE: When using OAuth authenticaiton, the READ/WRITE User scope is required by the GitHub APIs, otherwise you
     * will get a 404 NOT FOUND.
     *
     * @return Always non-null.
     * @throws IOException
     *             the io exception
     */
    public List<GHKey> getPublicKeys() throws IOException {
        return Collections
                .unmodifiableList(Arrays.asList(root.retrieve().withUrlPath("/user/keys").toArray(GHKey[].class)));
    }

    /**
     * Returns the read-only list of all the public verified keys of the current user.
     * <p>
     * Differently from the getPublicKeys() method, the retrieval of the user's verified public keys does not require
     * any READ/WRITE OAuth Scope to the user's profile.
     *
     * @return Always non-null.
     * @throws IOException
     *             the io exception
     */
    public List<GHVerifiedKey> getPublicVerifiedKeys() throws IOException {
        return Collections.unmodifiableList(Arrays
                .asList(root.retrieve().withUrlPath("/users/" + getLogin() + "/keys").toArray(GHVerifiedKey[].class)));
    }

    /**
     * Gets the organization that this user belongs to.
     *
     * @return the all organizations
     * @throws IOException
     *             the io exception
     */
    public GHPersonSet<GHOrganization> getAllOrganizations() throws IOException {
        GHPersonSet<GHOrganization> orgs = new GHPersonSet<GHOrganization>();
        Set<String> names = new HashSet<String>();
        for (GHOrganization o : root.retrieve().withUrlPath("/user/orgs").toArray(GHOrganization[].class)) {
            if (names.add(o.getLogin())) // in case of rumoured duplicates in the data
                orgs.add(root.getOrganization(o.getLogin()));
        }
        return orgs;
    }

    /**
     * Gets the all repositories this user owns (public and private).
     *
     * @return the all repositories
     * @throws IOException
     *             the io exception
     */
    public synchronized Map<String, GHRepository> getAllRepositories() throws IOException {
        Map<String, GHRepository> repositories = new TreeMap<String, GHRepository>();
        for (GHRepository r : listAllRepositories()) {
            repositories.put(r.getName(), r);
        }
        return Collections.unmodifiableMap(repositories);
    }

    /**
     * Lists up all repositories this user owns (public and private).
     *
     * Unlike {@link #getAllRepositories()}, this does not wait until all the repositories are returned. Repositories
     * are returned by GitHub API with a 30 items per page.
     */
    @Override
    public PagedIterable<GHRepository> listRepositories() {
        return listRepositories(30);
    }

    /**
     * List repositories that are accessible to the authenticated user (public and private) using the specified page
     * size.
     *
     * This includes repositories owned by the authenticated user, repositories that belong to other users where the
     * authenticated user is a collaborator, and other organizations' repositories that the authenticated user has
     * access to through an organization membership.
     *
     * @param pageSize
     *            size for each page of items returned by GitHub. Maximum page size is 100.
     *
     *            Unlike {@link #getRepositories()}, this does not wait until all the repositories are returned.
     */
    public PagedIterable<GHRepository> listRepositories(final int pageSize) {
        return listRepositories(pageSize, RepositoryListFilter.ALL);
    }

    /**
     * List repositories of a certain type that are accessible by current authenticated user using the specified page
     * size.
     *
     * @param pageSize
     *            size for each page of items returned by GitHub. Maximum page size is 100.
     * @param repoType
     *            type of repository returned in the listing
     * @return the paged iterable
     */
    public PagedIterable<GHRepository> listRepositories(final int pageSize, final RepositoryListFilter repoType) {
        return root.retrieve()
                .with("type", repoType)
                .asPagedIterable("/user/repos", GHRepository[].class, item -> item.wrap(root))
                .withPageSize(pageSize);
    }

    /**
     * List all repositories paged iterable.
     *
     * @return the paged iterable
     * @deprecated Use {@link #listRepositories()}
     */
    public PagedIterable<GHRepository> listAllRepositories() {
        return listRepositories();
    }

    /**
     * List your organization memberships
     *
     * @return the paged iterable
     */
    public PagedIterable<GHMembership> listOrgMemberships() {
        return listOrgMemberships(null);
    }

    /**
     * List your organization memberships
     *
     * @param state
     *            Filter by a specific state
     * @return the paged iterable
     */
    public PagedIterable<GHMembership> listOrgMemberships(final GHMembership.State state) {
        return root.retrieve()
                .with("state", state)
                .asPagedIterable("/user/memberships/orgs", GHMembership[].class, item -> item.wrap(root));
    }

    /**
     * Gets your membership in a specific organization.
     *
     * @param o
     *            the o
     * @return the membership
     * @throws IOException
     *             the io exception
     */
    public GHMembership getMembership(GHOrganization o) throws IOException {
        return root.retrieve().withUrlPath("/user/memberships/orgs/" + o.getLogin()).to(GHMembership.class).wrap(root);
    }

    // public void addEmails(Collection<String> emails) throws IOException {
    //// new Requester(root,ApiVersion.V3).withCredential().to("/user/emails");
    // root.retrieveWithAuth3()
    // }
}
