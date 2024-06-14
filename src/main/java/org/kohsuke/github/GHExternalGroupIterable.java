package org.kohsuke.github;

import java.util.Iterator;

import javax.annotation.Nonnull;

/**
 * Iterable for external group listing.
 *
 * @author Miguel Esteban Gutiérrez
 */
class GHExternalGroupIterable extends PagedIterable<GHExternalGroup> {

    private static final GHExternalGroup[] GH_EXTERNAL_GROUPS = new GHExternalGroup[0];

    private final GHOrganization owner;

    private final GitHubRequest request;

    private GHExternalGroups result;

    /**
     * Instantiates a new GH external groups iterable.
     *
     * @param owner
     *            the owner
     * @param requestBuilder
     *            the request builder
     */
    GHExternalGroupIterable(final GHOrganization owner, final GitHubRequest.Builder<?> requestBuilder) {
        this.owner = owner;
        this.request = requestBuilder.build();
    }

    /**
     * Iterator.
     *
     * @param pageSize
     *            the page size
     * @return the paged iterator
     */
    @Nonnull
    @Override
    public PagedIterator<GHExternalGroup> _iterator(int pageSize) {
        return new PagedIterator<>(
                adapt(GitHubPageIterator.create(owner.root().getClient(), GHExternalGroups.class, request, pageSize)),
                null);
    }

    /**
     * Adapt.
     *
     * @param base
     *            the base
     * @return the iterator
     */
    private Iterator<GHExternalGroup[]> adapt(final Iterator<GHExternalGroups> base) {
        return new Iterator<GHExternalGroup[]>() {
            public boolean hasNext() {
                try {
                    return base.hasNext();
                } catch (final GHException e) {
                    throw EnterpriseManagedSupport.forOrganization(owner).handleException(e).orElse(e);
                }
            }

            public GHExternalGroup[] next() {
                GHExternalGroups v = base.next();
                if (result == null) {
                    result = v;
                }
                v.getGroups().forEach(g -> g.wrapUp(owner));
                return v.getGroups().toArray(GH_EXTERNAL_GROUPS);
            }
        };
    }
}
