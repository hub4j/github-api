package org.kohsuke.github;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

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
     * Gets the repositories this user owns.
     */
    public synchronized Map<String,GHRepository> getRepositories() throws IOException {
        Map<String,GHRepository> repositories = new TreeMap<String, GHRepository>();
        for (int i=1; ; i++) {
            Map<String, GHRepository> map = root.retrieve3("/user/" + login + "/repos?per_page=100&page=" + i, JsonRepositories.class).wrap(root);
            repositories.putAll(map);
            if (map.isEmpty())  break;
        }

        return Collections.unmodifiableMap(repositories);
    }

    /**
     *
     * @return
     *      null if the repository was not found
     */
    public GHRepository getRepository(String name) throws IOException {
        try {
            return root.retrieve3("/repos/" + login + '/' + name, GHRepository.class).wrap(root);
        } catch (FileNotFoundException e) {
            return null;
        }
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
