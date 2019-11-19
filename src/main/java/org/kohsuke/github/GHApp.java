package org.kohsuke.github;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.kohsuke.github.Previews.MACHINE_MAN;

/**
 * A Github App.
 *
 * @author Paulo Miguel Almeida
 * @see GitHub#getApp() GitHub#getApp()
 */
public class GHApp extends GHObject {

    private GitHub root;
    private GHUser owner;
    private String name;
    private String description;
    private String externalUrl;
    private Map<String, String> permissions;
    private List<GHEvent> events;
    private long installationsCount;
    private String htmlUrl;

    /**
     * Gets owner.
     *
     * @return the owner
     */
    public GHUser getOwner() {
        return owner;
    }

    /**
     * Sets owner.
     *
     * @param owner
     *            the owner
     */
    public void setOwner(GHUser owner) {
        this.owner = owner;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name
     *            the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets description.
     *
     * @param description
     *            the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets external url.
     *
     * @return the external url
     */
    public String getExternalUrl() {
        return externalUrl;
    }

    /**
     * Sets external url.
     *
     * @param externalUrl
     *            the external url
     */
    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
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
     * Gets installations count.
     *
     * @return the installations count
     */
    public long getInstallationsCount() {
        return installationsCount;
    }

    /**
     * Sets installations count.
     *
     * @param installationsCount
     *            the installations count
     */
    public void setInstallationsCount(long installationsCount) {
        this.installationsCount = installationsCount;
    }

    public URL getHtmlUrl() {
        return GitHub.parseURL(htmlUrl);
    }

    /**
     * Gets permissions.
     *
     * @return the permissions
     */
    public Map<String, String> getPermissions() {
        return permissions;
    }

    /**
     * Sets permissions.
     *
     * @param permissions
     *            the permissions
     */
    public void setPermissions(Map<String, String> permissions) {
        this.permissions = permissions;
    }

    GHApp wrapUp(GitHub root) {
        this.root = root;
        return this;
    }

    /**
     * Obtains all the installations associated with this app.
     * <p>
     * You must use a JWT to access this endpoint.
     *
     * @return a list of App installations
     * @see <a href="https://developer.github.com/v3/apps/#list-installations">List installations</a>
     */
    @Preview
    @Deprecated
    public PagedIterable<GHAppInstallation> listInstallations() {
        return root.retrieve()
                .withPreview(MACHINE_MAN)
                .asPagedIterable("/app/installations", GHAppInstallation[].class, item -> item.wrapUp(root));
    }

    /**
     * Obtain an installation associated with this app.
     * <p>
     * You must use a JWT to access this endpoint.
     *
     * @param id
     *            Installation Id
     * @return a GHAppInstallation
     * @throws IOException
     *             on error
     * @see <a href="https://developer.github.com/v3/apps/#get-an-installation">Get an installation</a>
     */
    @Preview
    @Deprecated
    public GHAppInstallation getInstallationById(long id) throws IOException {
        return root.retrieve()
                .withPreview(MACHINE_MAN)
                .to(String.format("/app/installations/%d", id), GHAppInstallation.class)
                .wrapUp(root);
    }

    /**
     * Obtain an organization installation associated with this app.
     * <p>
     * You must use a JWT to access this endpoint.
     *
     * @param name
     *            Organization name
     * @return a GHAppInstallation
     * @throws IOException
     *             on error
     * @see <a href="https://developer.github.com/v3/apps/#get-an-organization-installation">Get an organization
     *      installation</a>
     */
    @Preview
    @Deprecated
    public GHAppInstallation getInstallationByOrganization(String name) throws IOException {
        return root.retrieve()
                .withPreview(MACHINE_MAN)
                .to(String.format("/orgs/%s/installation", name), GHAppInstallation.class)
                .wrapUp(root);
    }

    /**
     * Obtain an repository installation associated with this app.
     * <p>
     * You must use a JWT to access this endpoint.
     *
     * @param ownerName
     *            Organization or user name
     * @param repositoryName
     *            Repository name
     * @return a GHAppInstallation
     * @throws IOException
     *             on error
     * @see <a href="https://developer.github.com/v3/apps/#get-a-repository-installation">Get a repository
     *      installation</a>
     */
    @Preview
    @Deprecated
    public GHAppInstallation getInstallationByRepository(String ownerName, String repositoryName) throws IOException {
        return root.retrieve()
                .withPreview(MACHINE_MAN)
                .to(String.format("/repos/%s/%s/installation", ownerName, repositoryName), GHAppInstallation.class)
                .wrapUp(root);
    }

    /**
     * Obtain a user installation associated with this app.
     * <p>
     * You must use a JWT to access this endpoint.
     *
     * @param name
     *            user name
     * @return a GHAppInstallation
     * @throws IOException
     *             on error
     * @see <a href="https://developer.github.com/v3/apps/#get-a-user-installation">Get a user installation</a>
     */
    @Preview
    @Deprecated
    public GHAppInstallation getInstallationByUser(String name) throws IOException {
        return root.retrieve()
                .withPreview(MACHINE_MAN)
                .to(String.format("/users/%s/installation", name), GHAppInstallation.class)
                .wrapUp(root);
    }

}
