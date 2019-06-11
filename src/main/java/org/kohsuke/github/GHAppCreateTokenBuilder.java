package org.kohsuke.github;

import java.io.IOException;
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
public class GHAppCreateTokenBuilder {
    private final GitHub root;
    protected final Requester builder;
    private final String apiUrlTail;

    @Preview @Deprecated
    /*package*/ GHAppCreateTokenBuilder(GitHub root, String apiUrlTail, Map<String, String> permissions) {
        this.root = root;
        this.apiUrlTail = apiUrlTail;
        this.builder = new Requester(root);
        this.builder.with("permissions",permissions);
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
    public GHAppCreateTokenBuilder repositoryIds(List<Integer> repositoryIds) {
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
        return builder.method("POST").withPreview(MACHINE_MAN).to(apiUrlTail, GHAppInstallationToken.class).wrapUp(root);
    }

}
