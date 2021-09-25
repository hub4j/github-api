package org.kohsuke.github;

import java.net.MalformedURLException;
import java.util.Iterator;

import javax.annotation.Nonnull;

/**
 * Iterable for workflows listing.
 */
class GHWorkflowsIterable extends PagedIterable<GHWorkflow> {
    private final transient GHRepository owner;

    private GHWorkflowsPage result;

    public GHWorkflowsIterable(GHRepository owner) {
        this.owner = owner;
    }

    @Nonnull
    @Override
    public PagedIterator<GHWorkflow> _iterator(int pageSize) {
        try {
            GitHubRequest request = owner.root()
                    .createRequest()
                    .withUrlPath(owner.getApiTailUrl("actions/workflows"))
                    .build();

            return new PagedIterator<>(
                    adapt(GitHubPageIterator
                            .create(owner.root().getClient(), GHWorkflowsPage.class, request, pageSize)),
                    null);
        } catch (MalformedURLException e) {
            throw new GHException("Malformed URL", e);
        }
    }

    protected Iterator<GHWorkflow[]> adapt(final Iterator<GHWorkflowsPage> base) {
        return new Iterator<GHWorkflow[]>() {
            public boolean hasNext() {
                return base.hasNext();
            }

            public GHWorkflow[] next() {
                GHWorkflowsPage v = base.next();
                if (result == null) {
                    result = v;
                }
                return v.getWorkflows(owner);
            }
        };
    }
}
