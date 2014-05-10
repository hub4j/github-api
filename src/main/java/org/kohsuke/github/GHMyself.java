package org.kohsuke.github;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
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
     * Returns the read-only list of e-mail addresses configured for you.
     *
     * This corresponds to the stuff you configure in https://github.com/settings/emails,
     * and not to be confused with {@link #getEmail()} that shows your public e-mail address
     * set in https://github.com/settings/profile
     * 
     * @return
     *      Always non-null.
     */
    public List<GHEmail> getEmails() throws IOException {
        GHEmail[] addresses = root.retrieve().to("/user/emails", GHEmail[].class);
        return Collections.unmodifiableList(Arrays.asList(addresses));
    }

    /**
     * Returns the read-only list of all the pulic keys of the current user.
     *
     * NOTE: When using OAuth authenticaiton, the READ/WRITE User scope is
     * required by the GitHub APIs, otherwise you will get a 404 NOT FOUND.
     *
     * @return
     *      Always non-null.
     */
    public List<GHKey> getPublicKeys() throws IOException {
        return Collections.unmodifiableList(Arrays.asList(root.retrieve().to("/user/keys", GHKey[].class)));
    }

    /**
     * Returns the read-only list of all the public verified keys of the current user.
     *
     * Differently from the getPublicKeys() method, the retrieval of the user's
     * verified public keys does not require any READ/WRITE OAuth Scope to the
     * user's profile.
     *
     * @return
     *      Always non-null.
     */
  public List<GHVerifiedKey> getPublicVerifiedKeys() throws IOException {
    return Collections.unmodifiableList(Arrays.asList(root.retrieve().to(
        "/users/" + getLogin() + "/keys", GHVerifiedKey[].class)));
  }

    /**
     * Gets the organization that this user belongs to.
     */
    public GHPersonSet<GHOrganization> getAllOrganizations() throws IOException {
        GHPersonSet<GHOrganization> orgs = new GHPersonSet<GHOrganization>();
        Set<String> names = new HashSet<String>();
        for (GHOrganization o : root.retrieve().to("/user/orgs", GHOrganization[].class)) {
            if (names.add(o.getLogin()))    // in case of rumoured duplicates in the data
                orgs.add(root.getOrganization(o.getLogin()));
        }
        return orgs;
    }

    /**
     * Gets the all repositories this user owns (public and private).
     */
    public synchronized Map<String,GHRepository> getAllRepositories() throws IOException {
        Map<String,GHRepository> repositories = new TreeMap<String, GHRepository>();
        for (GHRepository r : listAllRepositories()) {
            repositories.put(r.getName(),r);
        }
        return Collections.unmodifiableMap(repositories);
    }

    /**
     * Lists up all repositories this user owns (public and private).
     *
     * Unlike {@link #getAllRepositories()}, this does not wait until all the repositories are returned.
     */
    public PagedIterable<GHRepository> listAllRepositories() {
        return new PagedIterable<GHRepository>() {
            public PagedIterator<GHRepository> iterator() {
                return new PagedIterator<GHRepository>(root.retrieve().asIterator("/user/repos", GHRepository[].class)) {
                    @Override
                    protected void wrapUp(GHRepository[] page) {
                        for (GHRepository c : page)
                            c.wrap(root);
                    }
                };
            }
        };
    }

//    public void addEmails(Collection<String> emails) throws IOException {
////        new Requester(root,ApiVersion.V3).withCredential().to("/user/emails");
//        root.retrieveWithAuth3()
//    }
}
