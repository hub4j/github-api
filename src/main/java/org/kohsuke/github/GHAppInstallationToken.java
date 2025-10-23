package org.kohsuke.github;

import java.util.*;

// TODO: Auto-generated Javadoc
/**
 * A Github App Installation Token.
 *
 * @author Paulo Miguel Almeida
 * @see GHAppInstallation#createToken() GHAppInstallation#createToken()
 */
public class GHAppInstallationToken extends GitHubInteractiveObject {

    private Map<String, String> permissions;

    private List<GHRepository> repositories;

    private GHRepositorySelection repositorySelection;
    private String token;
    /** The expires at. */
    protected String expires_at;
    /**
     * Create default GHAppInstallationToken instance
     */
    public GHAppInstallationToken() {
    }

    /**
     * Gets expires at.
     *
     * @return date when this token expires
     */
    public Date getExpiresAt() {
        return GitHubClient.parseDate(expires_at);
    }

    /**
     * Gets permissions.
     *
     * @return the permissions
     */
    public Map<String, String> getPermissions() {
        return Collections.unmodifiableMap(permissions);
    }

    /**
     * Gets repositories.
     *
     * @return the repositories
     */
    public List<GHRepository> getRepositories() {
        return GitHubClient.unmodifiableListOrNull(repositories);
    }

    /**
     * Gets repository selection.
     *
     * @return the repository selection
     */
    public GHRepositorySelection getRepositorySelection() {
        return repositorySelection;
    }

    /**
     * Gets token.
     *
     * @return the token
     */
    public String getToken() {
        return token;
    }
}
