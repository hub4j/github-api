package org.kohsuke.github;

class GHRepoHook extends GHHook {
    /**
     * Repository that the hook belongs to.
     */
    /*package*/ transient GHRepository repository;

    /*package*/ GHRepoHook wrap(GHRepository owner) {
        this.repository = owner;
        return this;
    }

    @Override
    GitHub getRoot() {
        return repository.getRoot();
    }

    @Override
    String getApiRoute() {
        return String.format("/repos/%s/%s/hooks/%d", repository.getOwnerName(), repository.getName(), id);
    }
}
