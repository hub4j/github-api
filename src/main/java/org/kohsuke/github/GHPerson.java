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
     * Gravatar ID of this user, like 0cb9832a01c22c083390f3c5dcb64105
     */
    public String getGravatarId() {
        return gravatar_id;
    }

    public String getLogin() {
        return login;
    }
}
