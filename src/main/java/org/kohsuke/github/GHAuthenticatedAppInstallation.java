package org.kohsuke.github;

import javax.annotation.Nonnull;

// TODO: Auto-generated Javadoc
/**
 * The Github App Installation corresponding to the installation token used in a client.
 *
 * @see GitHub#getInstallation() GitHub#getAuthenticatedAppInstallation()
 */
public class GHAuthenticatedAppInstallation extends GitHubInteractiveObject {

    private static class GHAuthenticatedAppInstallationRepositoryResult extends SearchResult<GHRepository> {
        private GHRepository[] repositories;

        @Override
        GHRepository[] getItems(GitHub root) {
            return repositories;
        }
    }

    /**
     * Instantiates a new GH authenticated app installation.
     *
     * @param root
     *            the root
     */
    protected GHAuthenticatedAppInstallation(@Nonnull GitHub root) {
        super(root);
    }

    /**
     * List repositories that this app installation can access.
     *
     * @return the paged iterable
     */
    public PagedSearchIterable<GHRepository> listRepositories() {
        return new PagedSearchIterable<>(root().createRequest()
                .withUrlPath("/installation/repositories")
                .toPaginatedEndpoint(GHAuthenticatedAppInstallationRepositoryResult.class, GHRepository.class, null));
    }

}
