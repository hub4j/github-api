package org.kohsuke.github;

class GHRepoHook extends GHHook {
    /**
     * Repository that the hook belongs to.
     */
    /*package*/ transient GHRepository repository;

    /*package*/ GHRepoHook wrap(GHRepository owner) {
        this.repository = owner;
        this.setRoot(repository.getRoot());
        return this;
    }

    @Override
    String getApiRoute() {
        return String.format("/repos/%s/%s/hooks/%d", repository.getOwnerName(), repository.getName(), id);
    }
}
