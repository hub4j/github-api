package org.kohsuke.github;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.kohsuke.github.Previews.MACHINE_MAN;

/**
 * Creates a access token for a GitHub App Installation
 *
 * @author Paulo Miguel Almeida
 * @see GHAppInstallation#createToken(Map) GHAppInstallation#createToken(Map)
 */
public class GHAppCreateTokenBuilder {
    private final GitHub root;
    protected final Requester builder;
    private final String apiUrlTail;

    @Preview
    @Deprecated
    GHAppCreateTokenBuilder(GitHub root, String apiUrlTail) {
        this.root = root;
        this.apiUrlTail = apiUrlTail;
        this.builder = root.createRequest();
        withPermissions(builder, permissions);
        this.builder = new Requester(root);
    }

    @Preview
    @Deprecated
    GHAppCreateTokenBuilder(GitHub root, String apiUrlTail, Map<String, GHPermissionType> permissions) {
        this(root, apiUrlTail);
        this.builder.withPermissions("permissions", permissions);
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
    @Preview
    @Deprecated
    public GHAppCreateTokenBuilder repositoryIds(List<Long> repositoryIds) {
        this.builder.with("repository_ids", repositoryIds);
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
    @Preview
    @Deprecated
    public GHAppInstallationToken create() throws IOException {
        return builder.method("POST")
                .withPreview(MACHINE_MAN)
                .withUrlPath(apiUrlTail)
                .fetch(GHAppInstallationToken.class)
                .wrapUp(root);
    }

    private static Requester withPermissions(Requester builder, Map<String, GHPermissionType> value) {
        Map<String, String> retMap = new HashMap<String, String>();
        for (Map.Entry<String, GHPermissionType> entry : value.entrySet()) {
            retMap.put(entry.getKey(), Requester.transformEnum(entry.getValue()));
        }
        return builder.with("permissions", retMap);
    }

}
