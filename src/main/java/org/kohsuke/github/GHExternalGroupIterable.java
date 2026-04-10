package org.kohsuke.github;

import org.jetbrains.annotations.NotNull;

/**
 * Iterable for external group listing.
 *
 * @author Miguel Esteban Gutiérrez
 */
class GHExternalGroupIterable extends PagedIterable<GHExternalGroup> {

    /**
     * Instantiates a new GH external groups iterable.
     *
     * @param owner
     *            the owner
     * @param requestBuilder
     *            the request builder
     */
    GHExternalGroupIterable(final GHOrganization owner, GitHubRequest.Builder<?> requestBuilder) {
        super(new PaginatedEndpoint<>(owner.root().getClient(),
                requestBuilder.build(),
                GHExternalGroupPage.class,
                GHExternalGroup.class,
                item -> item.wrapUp(owner)) {
            @NotNull @Override
            public PaginatedEndpointPages<GHExternalGroupPage, GHExternalGroup> pages() {
                return new PaginatedEndpointPages<>(client, pageType, request, pageSize, itemInitializer) {
                    @Override
                    public boolean hasNext() {
                        try {
                            return super.hasNext();
                        } catch (final GHException e) {
                            throw EnterpriseManagedSupport.forOrganization(owner).filterException(e).orElse(e);
                        }
                    }
                };
            }
        });
    }
}
