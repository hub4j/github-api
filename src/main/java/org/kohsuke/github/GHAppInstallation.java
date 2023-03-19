package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.kohsuke.github.internal.EnumUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.kohsuke.github.internal.Previews.GAMBIT;
import static org.kohsuke.github.internal.Previews.MACHINE_MAN;

// TODO: Auto-generated Javadoc
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
    private List<String> events;
    @JsonProperty("single_file_name")
    private String singleFileName;
    @JsonProperty("repository_selection")
    private GHRepositorySelection repositorySelection;
    private String htmlUrl;

    /**
     * Gets the html url.
     *
     * @return the html url
     */
    public URL getHtmlUrl() {
        return GitHubClient.parseURL(htmlUrl);
    }

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
     * Gets account.
     *
     * @return the account
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHUser getAccount() {
        return account;
    }

    /**
     * Sets account.
     *
     * @param account
     *            the account
     * @deprecated Do not use this method. It was added due to incomplete understanding of Jackson binding.
     */
    @Deprecated
    public void setAccount(GHUser account) {
        throw new RuntimeException("Do not use this method.");
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
     * @deprecated Do not use this method. It was added due to incomplete understanding of Jackson binding.
     */
    @Deprecated
    public void setAccessTokenUrl(String accessTokenUrl) {
        throw new RuntimeException("Do not use this method.");
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
     * List repositories that this app installation can access.
     *
     * @return the paged iterable
     * @deprecated This method cannot work on a {@link GHAppInstallation} retrieved from
     *             {@link GHApp#listInstallations()} (for example), except when resorting to unsupported hacks involving
     *             {@link GHAppInstallation#setRoot(GitHub)} to switch from an application client to an installation
     *             client. This method will be removed. You should instead use an installation client (with an
     *             installation token, not a JWT), retrieve a {@link GHAuthenticatedAppInstallation} from
     *             {@link GitHub#getInstallation()}, then call
     *             {@link GHAuthenticatedAppInstallation#listRepositories()}.
     */
    @Deprecated
    @Preview(MACHINE_MAN)
    public PagedSearchIterable<GHRepository> listRepositories() {
        GitHubRequest request;

        request = root().createRequest().withPreview(MACHINE_MAN).withUrlPath("/installation/repositories").build();

        return new PagedSearchIterable<>(root(), request, GHAppInstallationRepositoryResult.class);
    }

    private static class GHAppInstallationRepositoryResult extends SearchResult<GHRepository> {
        private GHRepository[] repositories;

        @Override
        GHRepository[] getItems(GitHub root) {
            return repositories;
        }
    }

    /**
     * Sets repositories url.
     *
     * @param repositoriesUrl
     *            the repositories url
     * @deprecated Do not use this method. It was added due to incomplete understanding of Jackson binding.
     */
    @Deprecated
    public void setRepositoriesUrl(String repositoriesUrl) {
        throw new RuntimeException("Do not use this method.");
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
     * @deprecated Do not use this method. It was added due to incomplete understanding of Jackson binding.
     */
    @Deprecated
    public void setAppId(long appId) {
        throw new RuntimeException("Do not use this method.");
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
     * @deprecated Do not use this method. It was added due to incomplete understanding of Jackson binding.
     */
    @Deprecated
    public void setTargetId(long targetId) {
        throw new RuntimeException("Do not use this method.");
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
     * @deprecated Do not use this method. It was added due to incomplete understanding of Jackson binding.
     */
    @Deprecated
    public void setTargetType(GHTargetType targetType) {
        throw new RuntimeException("Do not use this method.");
    }

    /**
     * Gets permissions.
     *
     * @return the permissions
     */
    public Map<String, GHPermissionType> getPermissions() {
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
    public void setPermissions(Map<String, GHPermissionType> permissions) {
        throw new RuntimeException("Do not use this method.");
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
     * Sets events.
     *
     * @param events
     *            the events
     * @deprecated Do not use this method. It was added due to incomplete understanding of Jackson binding.
     */
    @Deprecated
    public void setEvents(List<GHEvent> events) {
        throw new RuntimeException("Do not use this method.");
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
     * @deprecated Do not use this method. It was added due to incomplete understanding of Jackson binding.
     */
    @Deprecated
    public void setSingleFileName(String singleFileName) {
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
     * Delete a Github App installation
     * <p>
     * You must use a JWT to access this endpoint.
     *
     * @throws IOException
     *             on error
     * @see <a href="https://developer.github.com/v3/apps/#delete-an-installation">Delete an installation</a>
     */
    @Preview(GAMBIT)
    public void deleteInstallation() throws IOException {
        root().createRequest()
                .method("DELETE")
                .withPreview(GAMBIT)
                .withUrlPath(String.format("/app/installations/%d", getId()))
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
     * @return a GHAppCreateTokenBuilder instance
     * @deprecated Use {@link GHAppInstallation#createToken()} instead.
     */
    @BetaApi
    public GHAppCreateTokenBuilder createToken(Map<String, GHPermissionType> permissions) {
        return new GHAppCreateTokenBuilder(root(),
                String.format("/app/installations/%d/access_tokens", getId()),
                permissions);
    }

    /**
     * Starts a builder that creates a new App Installation Token.
     *
     * <p>
     * You use the returned builder to set various properties, then call {@link GHAppCreateTokenBuilder#create()} to
     * finally create an access token.
     *
     * @return a GHAppCreateTokenBuilder instance
     */
    @BetaApi
    public GHAppCreateTokenBuilder createToken() {
        return new GHAppCreateTokenBuilder(root(), String.format("/app/installations/%d/access_tokens", getId()));
    }

    /**
     * Shows whether the user or organization account actively subscribes to a plan listed by the authenticated GitHub
     * App. When someone submits a plan change that won't be processed until the end of their billing cycle, you will
     * also see the upcoming pending change.
     *
     * <p>
     * GitHub Apps must use a JWT to access this endpoint.
     * <p>
     * OAuth Apps must use basic authentication with their client ID and client secret to access this endpoint.
     *
     * @return a GHMarketplaceAccountPlan instance
     * @throws IOException
     * @see <a href=
     *      "https://docs.github.com/en/rest/apps/marketplace?apiVersion=2022-11-28#get-a-subscription-plan-for-an-account">Get
     *      a subscription plan for an account</a>
     */
    public GHMarketplaceAccountPlan getMarketplaceAccount() throws IOException {
        return new GHMarketplacePlanForAccountBuilder(root(), account.getId()).createRequest();
    }
}
