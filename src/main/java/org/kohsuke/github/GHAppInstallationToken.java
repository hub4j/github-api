package org.kohsuke.github;

import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.util.*;

/**
 * A Github App Installation Token.
 *
 * @author Paulo Miguel Almeida
 * @see GHAppInstallation#createToken(Map) GHAppInstallation#createToken(Map)
 */
public class GHAppInstallationToken extends GitHubInteractiveObject {
    private String token;
    protected String expires_at;
    private Map<String, String> permissions;
    private List<GHRepository> repositories;
    private GHRepositorySelection repositorySelection;

    /**
     * Sets root.
     *
     * @param root
     *            the root
     * @deprecated Do not use this method. It was added due to incomplete understanding of Jackson binding.
     */
    @Deprecated
    public void setRoot(GitHub root) {
        throw new RuntimeException("Do not use this method.");
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
     * Sets permissions.
     *
     * @param permissions
     *            the permissions
     * @deprecated Do not use this method. It was added due to incomplete understanding of Jackson binding.
     */
    @Deprecated
    public void setPermissions(Map<String, String> permissions) {
        throw new RuntimeException("Do not use this method.");
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
     * Sets token.
     *
     * @param token
     *            the token
     * @deprecated Do not use this method. It was added due to incomplete understanding of Jackson binding.
     */
    @Deprecated
    public void setToken(String token) {
        throw new RuntimeException("Do not use this method.");
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
     * Sets repositories.
     *
     * @param repositories
     *            the repositories
     * @deprecated Do not use this method. It was added due to incomplete understanding of Jackson binding.
     */
    @Deprecated
    public void setRepositories(List<GHRepository> repositories) {
        throw new RuntimeException("Do not use this method.");
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
     * Sets repository selection.
     *
     * @param repositorySelection
     *            the repository selection
     * @deprecated Do not use this method. It was added due to incomplete understanding of Jackson binding.
     */
    @Deprecated
    public void setRepositorySelection(GHRepositorySelection repositorySelection) {
        throw new RuntimeException("Do not use this method.");
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
