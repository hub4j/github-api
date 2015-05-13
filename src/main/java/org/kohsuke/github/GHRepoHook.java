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
    GitHub root() {
        return repository.root;
    }

    @Override
    String path() {
        return String.format("/repos/%s/%s/hooks/%d", repository.getOwnerName(), repository.getName(), id);
    }
}
