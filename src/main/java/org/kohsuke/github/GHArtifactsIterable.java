package org.kohsuke.github;

// TODO: Auto-generated Javadoc
/**
 * Iterable for artifacts listing.
 */
class GHArtifactsIterable extends PagedIterable<GHArtifact> {

    /**
     * Instantiates a new GH artifacts iterable.
     *
     * @param owner
     *            the owner
     * @param requestBuilder
     *            the request builder
     */
    public GHArtifactsIterable(GHRepository owner, GitHubRequest.Builder<?> requestBuilder) {
        super(new GitHubEndpointIterable<>(owner.root().getClient(),
                requestBuilder.build(),
                GHArtifactsPage.class,
                GHArtifact.class,
                item -> item.wrapUp(owner)));
    }
}
