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
 * @see GHApp#listInstallations() GHApp#listInstallations()
 * @see GHApp#getInstallationById(long) GHApp#getInstallationById(long)
 * @see GHApp#getInstallationByOrganization(String) GHApp#getInstallationByOrganization(String)
 * @see GHApp#getInstallationByRepository(String, String) GHApp#getInstallationByRepository(String, String)
 * @see GHApp#getInstallationByUser(String) GHApp#getInstallationByUser(String)
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

    /**
     * Gets root.
     *
     * @return the root
     */
    public GitHub getRoot() {
        return root;
    }

    /**
     * Sets root.
     *
     * @param root
     *            the root
     */
    public void setRoot(GitHub root) {
        this.root = root;
    }

    /**
     * Gets account.
     *
     * @return the account
     */
    public GHUser getAccount() {
        return account;
    }

    /**
     * Sets account.
     *
     * @param account
     *            the account
     */
    public void setAccount(GHUser account) {
        this.account = account;
    }

    /**
     * Gets access token url.
     *
     * @return the access token url
     */
    public String getAccessTokenUrl() {
        return accessTokenUrl;
    }

    /**
     * Sets access token url.
     *
     * @param accessTokenUrl
     *            the access token url
     */
    public void setAccessTokenUrl(String accessTokenUrl) {
        this.accessTokenUrl = accessTokenUrl;
    }

    /**
     * Gets repositories url.
     *
     * @return the repositories url
     */
    public String getRepositoriesUrl() {
        return repositoriesUrl;
    }

    /**
     * Sets repositories url.
     *
     * @param repositoriesUrl
     *            the repositories url
     */
    public void setRepositoriesUrl(String repositoriesUrl) {
        this.repositoriesUrl = repositoriesUrl;
    }

    /**
     * Gets app id.
     *
     * @return the app id
     */
    public long getAppId() {
        return appId;
    }

    /**
     * Sets app id.
     *
     * @param appId
     *            the app id
     */
    public void setAppId(long appId) {
        this.appId = appId;
    }

    /**
     * Gets target id.
     *
     * @return the target id
     */
    public long getTargetId() {
        return targetId;
    }

    /**
     * Sets target id.
     *
     * @param targetId
     *            the target id
     */
    public void setTargetId(long targetId) {
        this.targetId = targetId;
    }

    /**
     * Gets target type.
     *
     * @return the target type
     */
    public GHTargetType getTargetType() {
        return targetType;
    }

    /**
     * Sets target type.
     *
     * @param targetType
     *            the target type
     */
    public void setTargetType(GHTargetType targetType) {
        this.targetType = targetType;
    }

    /**
     * Gets permissions.
     *
     * @return the permissions
     */
    public Map<String, GHPermissionType> getPermissions() {
        return permissions;
    }

    /**
     * Sets permissions.
     *
     * @param permissions
     *            the permissions
     */
    public void setPermissions(Map<String, GHPermissionType> permissions) {
        this.permissions = permissions;
    }

    /**
     * Gets events.
     *
     * @return the events
     */
    public List<GHEvent> getEvents() {
        return events;
    }

    /**
     * Sets events.
     *
     * @param events
     *            the events
     */
    public void setEvents(List<GHEvent> events) {
        this.events = events;
    }

    /**
     * Gets single file name.
     *
     * @return the single file name
     */
    public String getSingleFileName() {
        return singleFileName;
    }

    /**
     * Sets single file name.
     *
     * @param singleFileName
     *            the single file name
     */
    public void setSingleFileName(String singleFileName) {
        this.singleFileName = singleFileName;
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
     */
    public void setRepositorySelection(GHRepositorySelection repositorySelection) {
        this.repositorySelection = repositorySelection;
    }

    GHAppInstallation wrapUp(GitHub root) {
        this.root = root;
        return this;
    }

    /**
     * Delete a Github App installation
     * <p>
     * You must use a JWT to access this endpoint.
     *
     * @throws IOException
     *             on error
     * @see <a href="https://developer.github.com/v3/apps/#delete-an-installation">Delete an installation</a>
     */
    @Preview
    @Deprecated
    public void deleteInstallation() throws IOException {
        root.createRequest()
                .method("DELETE")
                .withPreview(GAMBIT)
                .withUrlPath(String.format("/app/installations/%d", id))
                .send();
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

    /**
     * Starts a builder that creates a new App Installation Token.
     *
     * <p>
     * You use the returned builder to set various properties, then call {@link GHAppCreateTokenBuilder#create()} to
     * finally create an access token.
     *
     * @return a GHAppCreateTokenBuilder on error
     */
    @Preview
    @Deprecated
    public GHAppCreateTokenBuilder createToken() {
        return new GHAppCreateTokenBuilder(root, String.format("/app/installations/%d/access_tokens", id));
    }
}
