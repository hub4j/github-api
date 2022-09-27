package org.kohsuke.github;

/**
 * A public key for the given repository
 *
 * @author Aditya Bansal
 */
class GHRepositoryPublicKey extends GHPublicKey {
    /**
     * Repository that the secret belongs to.
     */
    transient GHRepository repository;

    GHRepositoryPublicKey wrap(GHRepository owner) {
        this.repository = owner;
        return this;
    }

    @Override
    GitHub root() {
        return repository.root();
    }

    @Override
    String getApiRoute() {
        return String
                .format("/repos/%s/%s/actions/secrets/public-key", repository.getOwnerName(), repository.getName());
    }
}
