package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.kohsuke.github.Previews.MACHINE_MAN;

/**
 * A Github App.
 *
 * @author Paulo Miguel Almeida
 *
 * @see GitHub#getApp()
 */

public class GHApp extends GHObject {

    private GitHub root;
    private GHUser owner;
    private String name;
    private String description;
    @JsonProperty("external_url")
    private String externalUrl;
    private Map<String,String> permissions;
    private List<GHEvent> events;
    @JsonProperty("installations_count")
    private long installationsCount;
    @JsonProperty("html_url")
    private String htmlUrl;


    public GHUser getOwner() {
        return owner;
    }

    public void setOwner(GHUser owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
    }

    public List<GHEvent> getEvents() {
        return events;
    }

    public void setEvents(List<GHEvent> events) {
        this.events = events;
    }

    public long getInstallationsCount() {
        return installationsCount;
    }

    public void setInstallationsCount(long installationsCount) {
        this.installationsCount = installationsCount;
    }

    public URL getHtmlUrl() {
        return GitHub.parseURL(htmlUrl);
    }

    public Map<String, String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Map<String, String> permissions) {
        this.permissions = permissions;
    }

    /*package*/ GHApp wrapUp(GitHub root) {
        this.root = root;
        return this;
    }

    /**
     * Obtains all the installations associated with this app.
     *
     * You must use a JWT to access this endpoint.
     *
     * @see <a href="https://developer.github.com/v3/apps/#list-installations">List installations</a>
     */
    @Preview @Deprecated
    public PagedIterable<GHAppInstallation> listInstallations() {
        return root.retrieve().withPreview(MACHINE_MAN)
            .asPagedIterable(
                "/app/installations",
                GHAppInstallation[].class,
                item -> item.wrapUp(root) );
    }

    /**
     * Obtain an installation associated with this app
     * @param id - Installation Id
     *
     * You must use a JWT to access this endpoint.
     *
     * @see <a href="https://developer.github.com/v3/apps/#get-an-installation">Get an installation</a>
     */
    @Preview @Deprecated
    public GHAppInstallation getInstallationById(long id) throws IOException {
        return root.retrieve().withPreview(MACHINE_MAN).to(String.format("/app/installations/%d", id), GHAppInstallation.class).wrapUp(root);
    }

    /**
     * Obtain an organization installation associated with this app
     * @param name - Organization name
     *
     * You must use a JWT to access this endpoint.
     *
     * @see <a href="https://developer.github.com/v3/apps/#get-an-organization-installation">Get an organization installation</a>
     */
    @Preview @Deprecated
    public GHAppInstallation getInstallationByOrganization(String name) throws IOException {
        return root.retrieve().withPreview(MACHINE_MAN).to(String.format("/orgs/%s/installation", name), GHAppInstallation.class).wrapUp(root);
    }

    /**
     * Obtain an repository installation associated with this app
     * @param ownerName - Organization or user name
     * @param repositoryName - Repository name
     *
     * You must use a JWT to access this endpoint.
     *
     * @see <a href="https://developer.github.com/v3/apps/#get-a-repository-installation">Get a repository installation</a>
     */
    @Preview @Deprecated
    public GHAppInstallation getInstallationByRepository(String ownerName, String repositoryName) throws IOException {
        return root.retrieve().withPreview(MACHINE_MAN).to(String.format("/repos/%s/%s/installation", ownerName, repositoryName), GHAppInstallation.class).wrapUp(root);
    }

    /**
     * Obtain a user installation associated with this app
     * @param name - user name
     *
     * You must use a JWT to access this endpoint.
     *
     * @see <a href="https://developer.github.com/v3/apps/#get-a-user-installation">Get a user installation</a>
     */
    @Preview @Deprecated
    public GHAppInstallation getInstallationByUser(String name) throws IOException {
        return root.retrieve().withPreview(MACHINE_MAN).to(String.format("/users/%s/installation", name), GHAppInstallation.class).wrapUp(root);
    }

}

