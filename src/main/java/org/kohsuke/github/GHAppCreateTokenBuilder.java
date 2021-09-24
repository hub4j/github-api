package org.kohsuke.github;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.kohsuke.github.internal.Previews.MACHINE_MAN;

/**
 * Creates a access token for a GitHub App Installation
 *
 * @author Paulo Miguel Almeida
 * @see GHAppInstallation#createToken(Map) GHAppInstallation#createToken(Map)
 * @see GHAppInstallation#createToken() GHAppInstallation#createToken()
 */
public class GHAppCreateTokenBuilder extends GitHubInteractiveObject {
    protected final Requester builder;
    private final String apiUrlTail;

    @BetaApi
    GHAppCreateTokenBuilder(GitHub root, String apiUrlTail) {
        super(root);
        this.apiUrlTail = apiUrlTail;
        this.builder = root.createRequest();
    }

    @BetaApi
    GHAppCreateTokenBuilder(GitHub root, String apiUrlTail, Map<String, GHPermissionType> permissions) {
        this(root, apiUrlTail);
        permissions(permissions);
    }

    /**
     * By default the installation token has access to all repositories that the installation can access. To restrict
     * the access to specific repositories, you can provide the repository_ids when creating the token. When you omit
     * repository_ids, the response does not contain neither the repositories nor the permissions key.
     *
     * @param repositoryIds
     *            Array containing the repositories Ids
     * @return a GHAppCreateTokenBuilder
     */
    @BetaApi
    public GHAppCreateTokenBuilder repositoryIds(List<Long> repositoryIds) {
        this.builder.with("repository_ids", repositoryIds);
        return this;
    }

    /**
     * Set the permissions granted to the access token. The permissions object includes the permission names and their
     * access type.
     *
     * @param permissions
     *            Map containing the permission names and types.
     * @return a GHAppCreateTokenBuilder
     */
    @BetaApi
    public GHAppCreateTokenBuilder permissions(Map<String, GHPermissionType> permissions) {
        Map<String, String> retMap = new HashMap<>();
        for (Map.Entry<String, GHPermissionType> entry : permissions.entrySet()) {
            retMap.put(entry.getKey(), GitHubRequest.transformEnum(entry.getValue()));
        }
        builder.with("permissions", retMap);
        return this;
    }

    /**
     * Creates an app token with all the parameters.
     * <p>
     * You must use a JWT to access this endpoint.
     *
     * @return a GHAppInstallationToken
     * @throws IOException
     *             on error
     */
    @Preview(MACHINE_MAN)
    public GHAppInstallationToken create() throws IOException {
        return builder.method("POST")
                .withPreview(MACHINE_MAN)
                .withUrlPath(apiUrlTail)
                .fetch(GHAppInstallationToken.class);
    }

}
