package org.kohsuke.github;

import com.infradna.tool.bridge_method_injector.WithBridgeMethods;

import java.time.Instant;
import java.util.*;

// TODO: Auto-generated Javadoc
/**
 * A Github App Installation Token.
 *
 * @author Paulo Miguel Almeida
 * @see GHAppInstallation#createToken() GHAppInstallation#createToken()
 */
public class GHAppInstallationToken extends GitHubInteractiveObject {

    /**
     * Create default GHAppInstallationToken instance
     */
    public GHAppInstallationToken() {
    }

    private String token;

    /** The expires at. */
    protected String expiresAt;
    private Map<String, String> permissions;
    private List<GHRepository> repositories;
    private GHRepositorySelection repositorySelection;

    /**
     * Gets permissions.
     *
     * @return the permissions
     */
    public Map<String, String> getPermissions() {
        return Collections.unmodifiableMap(permissions);
    }

    /**
     * Gets token.
     *
     * @return the token
     */
    public String getToken() {
        return token;
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
     * Gets expires at.
     *
     * @return date when this token expires
     */
    @WithBridgeMethods(value = Date.class, adapterMethod = "instantToDate")
    public Instant getExpiresAt() {
        return GitHubClient.parseInstant(expiresAt);
    }
}
