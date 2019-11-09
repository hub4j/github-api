package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * A Github App Installation Token.
 *
 * @author Paulo Miguel Almeida
 *
 * @see GHAppInstallation#createToken(Map)
 */

public class GHAppInstallationToken extends GHObjectBase{

    private String token;
    protected String expires_at;
    private Map<String, String> permissions;
    private List<GHRepository> repositories;
    @JsonProperty("repository_selection")
    private GHRepositorySelection repositorySelection;

    @Override
    public GitHub getRoot() {
        return super.getRoot();
    }

    @Override
    public void setRoot(GitHub root) {
        //TODO: needs fixing
        //this.root = root;
        super.setRoot(root);
    }

    public Map<String, String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Map<String, String> permissions) {
        this.permissions = permissions;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public List<GHRepository> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<GHRepository> repositories) {
        this.repositories = repositories;
    }

    public GHRepositorySelection getRepositorySelection() {
        return repositorySelection;
    }

    public void setRepositorySelection(GHRepositorySelection repositorySelection) {
        this.repositorySelection = repositorySelection;
    }

    /**
     * When was this tokens expires?
     */
    @WithBridgeMethods(value=String.class, adapterMethod="expiresAtStr")
    public Date getExpiresAt() throws IOException {
        return GitHub.parseDate(expires_at);
    }

    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "Bridge method of getExpiresAt")
    private Object expiresAtStr(Date id, Class type) {
        return expires_at;
    }
}
