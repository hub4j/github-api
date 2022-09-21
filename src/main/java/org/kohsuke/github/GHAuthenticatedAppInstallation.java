package org.kohsuke.github;

import javax.annotation.Nonnull;

import static org.kohsuke.github.internal.Previews.MACHINE_MAN;

/**
 * The Github App Installation corresponding to the installation token used in a client.
 *
 * @see GitHub#getInstallation() GitHub#getAuthenticatedAppInstallation()
 */
public class GHAuthenticatedAppInstallation extends GitHubInteractiveObject {
    protected GHAuthenticatedAppInstallation(@Nonnull GitHub root) {
        super(root);
    }

    /**
     * List repositories that this app installation can access.
     *
     * @return the paged iterable
     */
    @Preview(MACHINE_MAN)
    public PagedSearchIterable<GHRepository> listRepositories() {
        GitHubRequest request;

        request = root().createRequest().withPreview(MACHINE_MAN).withUrlPath("/installation/repositories").build();

        return new PagedSearchIterable<>(root(), request, GHAuthenticatedAppInstallationRepositoryResult.class);
    }

    private static class GHAuthenticatedAppInstallationRepositoryResult extends SearchResult<GHRepository> {
        private GHRepository[] repositories;

        @Override
        GHRepository[] getItems(GitHub root) {
            return repositories;
        }
    }

}
