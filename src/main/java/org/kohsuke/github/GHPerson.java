package org.kohsuke.github;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import static org.kohsuke.github.GitHub.*;

/**
 * Common part of {@link GHUser} and {@link GHOrganization}.
 * 
 * @author Kohsuke Kawaguchi
 */
public abstract class GHPerson {
    /*package almost final*/ GitHub root;

    protected String gravatar_id,login;

    protected int public_gist_count,public_repo_count,following_count,id;

    /**
     * Repositories that this user owns.
     */
    private transient Map<String,GHRepository> repositories;

    /**
     * Gets the repositories this user owns.
     */
    public synchronized Map<String,GHRepository> getRepositories() throws IOException {
        if (repositories==null) {
            repositories = Collections.synchronizedMap(new TreeMap<String, GHRepository>());
            for (GHRepository r : root.retrieve("/repos/show/" + login, JsonRepositories.class).repositories) {
                r.root = root;
                repositories.put(r.getName(),r);
            }
        }

        return Collections.unmodifiableMap(repositories);
    }

    /**
     * Fetches the repository of the given name from GitHub, and return it.
     */
    protected GHRepository refreshRepository(String name) throws IOException {
        if (repositories==null) getRepositories(); // fetch the base first
        GHRepository r = root.retrieve("/repos/show/" + login + '/' + name, JsonRepository.class).wrap(root);
        repositories.put(name,r);
        return r;
    }

    public GHRepository getRepository(String name) throws IOException {
        return getRepositories().get(name);
    }

    /**
     * Gravatar ID of this user, like 0cb9832a01c22c083390f3c5dcb64105
     */
    public String getGravatarId() {
        return gravatar_id;
    }

    public String getLogin() {
        return login;
    }
}
