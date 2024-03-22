package org.kohsuke.github;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.kohsuke.github.internal.EnumUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// TODO: Auto-generated Javadoc
/**
 * A Github App.
 *
 * @author Paulo Miguel Almeida
 * @see GitHub#getApp() GitHub#getApp()
 */
public class GHApp extends GHObject {

    private GHUser owner;
    private String name;
    private String slug;
    private String description;
    private String externalUrl;
    private Map<String, String> permissions;
    private List<String> events;
    private long installationsCount;
    private String htmlUrl;

    /**
     * Gets owner.
     *
     * @return the owner
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHUser getOwner() {
        return owner;
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
     * Gets the slug name of the GitHub app.
     *
     * @return the slug name of the GitHub app
     */
    public String getSlug() {
        return slug;
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
     * Gets external url.
     *
     * @return the external url
     */
    public String getExternalUrl() {
        return externalUrl;
    }

    /**
     * Gets events.
     *
     * @return the events
     */
    public List<GHEvent> getEvents() {
        return events.stream()
                .map(e -> EnumUtils.getEnumOrDefault(GHEvent.class, e, GHEvent.UNKNOWN))
                .collect(Collectors.toList());
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
     * Gets the html url.
     *
     * @return the html url
     */
    public URL getHtmlUrl() {
        return GitHubClient.parseURL(htmlUrl);
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
     * Obtains all the installations associated with this app.
     * <p>
     * You must use a JWT to access this endpoint.
     *
     * @return a list of App installations
     * @see <a href="https://developer.github.com/v3/apps/#list-installations">List installations</a>
     */
    public PagedIterable<GHAppInstallation> listInstallations() {
        return listInstallations(null);
    }

    /**
     * Obtains all the installations associated with this app since a given date.
     * <p>
     * You must use a JWT to access this endpoint.
     *
     * @param since
     *            - Allows users to get installations that have been updated since a given date.
     * @return a list of App installations since a given time.
     * @see <a href="https://developer.github.com/v3/apps/#list-installations">List installations</a>
     */
    public PagedIterable<GHAppInstallation> listInstallations(final Date since) {
        Requester requester = root().createRequest().withUrlPath("/app/installations");
        if (since != null) {
            requester.with("since", GitHubClient.printDate(since));
        }
        return requester.toIterable(GHAppInstallation[].class, null);
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
    public GHAppInstallation getInstallationById(long id) throws IOException {
        return root().createRequest()
                .withUrlPath(String.format("/app/installations/%d", id))
                .fetch(GHAppInstallation.class);
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
    public GHAppInstallation getInstallationByOrganization(String name) throws IOException {
        return root().createRequest()
                .withUrlPath(String.format("/orgs/%s/installation", name))
                .fetch(GHAppInstallation.class);
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
    public GHAppInstallation getInstallationByRepository(String ownerName, String repositoryName) throws IOException {
        return root().createRequest()
                .withUrlPath(String.format("/repos/%s/%s/installation", ownerName, repositoryName))
                .fetch(GHAppInstallation.class);
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
    public GHAppInstallation getInstallationByUser(String name) throws IOException {
        return root().createRequest()
                .withUrlPath(String.format("/users/%s/installation", name))
                .fetch(GHAppInstallation.class);
    }

}
