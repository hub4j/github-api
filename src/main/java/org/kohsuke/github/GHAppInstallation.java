package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.kohsuke.github.Previews.GAMBIT;

/**
 * A Github App Installation.
 *
 * @author Paulo Miguel Almeida
 *
 * @see GHApp#listInstallations()
 * @see GHApp#getInstallationById(long)
 * @see GHApp#getInstallationByOrganization(String)
 * @see GHApp#getInstallationByRepository(String, String)
 * @see GHApp#getInstallationByUser(String)
 */

public class GHAppInstallation extends GHObject {
    private GitHub root;
    private GHUser account;

    @JsonProperty("access_tokens_url")
    private String accessTokenUrl;
    @JsonProperty("repositories_url")
    private String repositoriesUrl;
    @JsonProperty("app_id")
    private long appId;
    @JsonProperty("target_id")
    private long targetId;
    @JsonProperty("target_type")
    private GHTargetType targetType;
    private Map<String, GHPermissionType> permissions;
    private List<GHEvent> events;
    @JsonProperty("single_file_name")
    private String singleFileName;
    @JsonProperty("repository_selection")
    private GHRepositorySelection repositorySelection;
    private String htmlUrl;

    public URL getHtmlUrl() {
        return GitHub.parseURL(htmlUrl);
    }

    public GitHub getRoot() {
        return root;
    }

    public void setRoot(GitHub root) {
        this.root = root;
    }

    public GHUser getAccount() {
        return account;
    }

    public void setAccount(GHUser account) {
        this.account = account;
    }

    public String getAccessTokenUrl() {
        return accessTokenUrl;
    }

    public void setAccessTokenUrl(String accessTokenUrl) {
        this.accessTokenUrl = accessTokenUrl;
    }

    public String getRepositoriesUrl() {
        return repositoriesUrl;
    }

    public void setRepositoriesUrl(String repositoriesUrl) {
        this.repositoriesUrl = repositoriesUrl;
    }

    public long getAppId() {
        return appId;
    }

    public void setAppId(long appId) {
        this.appId = appId;
    }

    public long getTargetId() {
        return targetId;
    }

    public void setTargetId(long targetId) {
        this.targetId = targetId;
    }

    public GHTargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(GHTargetType targetType) {
        this.targetType = targetType;
    }

    public Map<String, GHPermissionType> getPermissions() {
        return permissions;
    }

    public void setPermissions(Map<String, GHPermissionType> permissions) {
        this.permissions = permissions;
    }

    public List<GHEvent> getEvents() {
        return events;
    }

    public void setEvents(List<GHEvent> events) {
        this.events = events;
    }

    public String getSingleFileName() {
        return singleFileName;
    }

    public void setSingleFileName(String singleFileName) {
        this.singleFileName = singleFileName;
    }

    public GHRepositorySelection getRepositorySelection() {
        return repositorySelection;
    }

    public void setRepositorySelection(GHRepositorySelection repositorySelection) {
        this.repositorySelection = repositorySelection;
    }

    /* package */ GHAppInstallation wrapUp(GitHub root) {
        this.root = root;
        return this;
    }

    /**
     * Delete a Github App installation
     *
     * You must use a JWT to access this endpoint.
     *
     * @throws IOException
     *             on error
     * @see <a href="https://developer.github.com/v3/apps/#delete-an-installation">Delete an installation</a>
     */
    @Preview
    @Deprecated
    public void deleteInstallation() throws IOException {
        root.retrieve().method("DELETE").withPreview(GAMBIT).to(String.format("/app/installations/%d", id));
    }

    /**
     * Starts a builder that creates a new App Installation Token.
     *
     * <p>
     * You use the returned builder to set various properties, then call {@link GHAppCreateTokenBuilder#create()} to
     * finally create an access token.
     *
     * @param permissions
     *            map of permissions for the created token
     * @return a GHAppCreateTokenBuilder on error
     */
    @Preview
    @Deprecated
    public GHAppCreateTokenBuilder createToken(Map<String, GHPermissionType> permissions) {
        return new GHAppCreateTokenBuilder(root, String.format("/app/installations/%d/access_tokens", id), permissions);
    }
}
