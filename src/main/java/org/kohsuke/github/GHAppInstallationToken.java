package org.kohsuke.github;

import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.util.*;

// TODO: Auto-generated Javadoc
/**
 * A Github App Installation Token.
 *
 * @author Paulo Miguel Almeida
 * @see GHAppInstallation#createToken(Map) GHAppInstallation#createToken(Map)
 */
public class GHAppInstallationToken extends GitHubInteractiveObject {
    private String token;

    /** The expires at. */
    protected String expires_at;
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
     * @throws IOException
     *             on error
     */
    @WithBridgeMethods(value = String.class, adapterMethod = "expiresAtStr")
    public Date getExpiresAt() throws IOException {
        return GitHubClient.parseDate(expires_at);
    }

    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "Bridge method of getExpiresAt")
    private Object expiresAtStr(Date id, Class type) {
        return expires_at;
    }
}
