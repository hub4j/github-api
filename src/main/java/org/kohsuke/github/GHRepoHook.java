package org.kohsuke.github;

// TODO: Auto-generated Javadoc
/**
 * The Class GHRepoHook.
 */
class GHRepoHook extends GHHook {
    /**
     * Repository that the hook belongs to.
     */
    transient GHRepository repository;

    /**
     * Wrap.
     *
     * @param owner
     *            the owner
     * @return the GH repo hook
     */
    GHRepoHook wrap(GHRepository owner) {
        this.repository = owner;
        return this;
    }

    /**
     * Root.
     *
     * @return the git hub
     */
    @Override
    GitHub root() {
        return repository.root();
    }

    /**
     * Gets the api route.
     *
     * @return the api route
     */
    @Override
    String getApiRoute() {
        return String.format("/repos/%s/%s/hooks/%d", repository.getOwnerName(), repository.getName(), getId());
    }
}
