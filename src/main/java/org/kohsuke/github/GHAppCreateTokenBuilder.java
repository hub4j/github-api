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
 *
 * @see GHAppInstallation#createToken(Map)
 */
public class GHAppCreateTokenBuilder extends GHObjectBase {
    protected final Requester builder;
    private final String apiUrlTail;

    @Preview @Deprecated
    /*package*/ GHAppCreateTokenBuilder(GitHub root, String apiUrlTail, Map<String, GHPermissionType> permissions) {
        this.setRoot(root);
        this.apiUrlTail = apiUrlTail;
        this.builder = createRequester();
        withPermissions(builder, permissions);
    }

    /**
     * By default the installation token has access to all repositories that the installation can access. To restrict
     * the access to specific repositories, you can provide the repository_ids when creating the token. When you omit
     * repository_ids, the response does not contain neither the repositories nor the permissions key.
     *
     * @param repositoryIds - Array containing the repositories Ids
     *
     */
    @Preview @Deprecated
    public GHAppCreateTokenBuilder repositoryIds(List<Long> repositoryIds) {
        this.builder.with("repository_ids",repositoryIds);
        return this;
    }

    /**
     * Creates an app token with all the parameters.
     *
     * You must use a JWT to access this endpoint.
     */
    @Preview @Deprecated
    public GHAppInstallationToken create() throws IOException {
        return builder.method("POST").withPreview(MACHINE_MAN).to(apiUrlTail, GHAppInstallationToken.class).wrapUp(getRoot());
    }

    private static Requester withPermissions(Requester builder, Map<String, GHPermissionType> value) {
        Map<String,String> retMap = new HashMap<String, String>();
        for (Map.Entry<String, GHPermissionType> entry : value.entrySet()) {
            retMap.put(entry.getKey(), Requester.transformEnum(entry.getValue()));
        }
        return builder.with("permissions", retMap);
    }

}
