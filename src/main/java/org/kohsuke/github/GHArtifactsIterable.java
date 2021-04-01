package org.kohsuke.github;

import java.net.MalformedURLException;
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
        try {
            this.request = requestBuilder.build();
        } catch (MalformedURLException e) {
            throw new GHException("Malformed URL", e);
        }
    }

    @Nonnull
    @Override
    public PagedIterator<GHArtifact> _iterator(int pageSize) {
        return new PagedIterator<>(
                adapt(GitHubPageIterator.create(owner.getRoot().getClient(), GHArtifactsPage.class, request, pageSize)),
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
