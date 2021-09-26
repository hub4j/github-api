package org.kohsuke.github;

import java.util.Iterator;

import javax.annotation.Nonnull;

/**
 * Iterable for artifacts listing.
 */
class GHArtifactsIterable extends PagedIterable<GHArtifact> {
    private final transient GHRepository owner;
    private final GitHubRequest request;

    private GHArtifactsPage result;

    public GHArtifactsIterable(GHRepository owner, GitHubRequest.Builder<?> requestBuilder) {
        this.owner = owner;
        this.request = requestBuilder.build();
    }

    @Nonnull
    @Override
    public PagedIterator<GHArtifact> _iterator(int pageSize) {
        return new PagedIterator<>(
                adapt(GitHubPageIterator.create(owner.root().getClient(), GHArtifactsPage.class, request, pageSize)),
                null);
    }

    protected Iterator<GHArtifact[]> adapt(final Iterator<GHArtifactsPage> base) {
        return new Iterator<GHArtifact[]>() {
            public boolean hasNext() {
                return base.hasNext();
            }

            public GHArtifact[] next() {
                GHArtifactsPage v = base.next();
                if (result == null) {
                    result = v;
                }
                return v.getArtifacts(owner);
            }
        };
    }
}
