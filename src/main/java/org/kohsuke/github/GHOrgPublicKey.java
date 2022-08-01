package org.kohsuke.github;

/**
 * A GitHub organization's public key.
 *
 * @author João Almeida
 */
class GHOrgPublicKey extends GHPublicKey {
    /**
     * Organization that the public key belongs to.
     */
    transient GHOrganization organization;

    GHOrgPublicKey wrap(GHOrganization owner) {
        this.organization = owner;
        return this;
    }

    @Override
    GitHub root() {
        return organization.root();
    }

    @Override
    String getApiRoute() {
        return String.format("/orgs/%s/actions/secrets/public-key", organization.getLogin());
    }
}
