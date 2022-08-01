package org.kohsuke.github;

import java.io.IOException;
import java.util.*;

/**
 * Utility class for managing public keys; removes duplication between GHOrganization and GHRepository functionality
 */
class GHPublicKeys {

    static abstract class Context extends GitHubInteractiveObject {

        private Context(GitHub root) {
            super(root);
        }

        /**
         * Gets public Key.
         *
         * @return the public key
         * @throws IOException
         *             the io exception
         */
        public GHPublicKey getPublicKey() throws IOException {
            GHPublicKey key = root().createRequest().withUrlPath(collection()).fetch(clazz());
            return wrap(key);
        }

        abstract String collection();

        abstract Class<? extends GHPublicKey[]> collectionClass();

        abstract Class<? extends GHPublicKey> clazz();

        abstract GHPublicKey wrap(GHPublicKey key);

    }

    private static class RepoContext extends Context {
        private final GHRepository repository;
        private final GHUser owner;

        private RepoContext(GHRepository repository, GHUser owner) {
            super(repository.root());
            this.repository = repository;
            this.owner = owner;
        }

        @Override
        String collection() {
            return String.format("/repos/%s/%s/actions/secrets/public-key", owner.getLogin(), repository.getName());
        }

        @Override
        Class<? extends GHPublicKey[]> collectionClass() {
            return GHRepositoryPublicKey[].class;
        }

        @Override
        Class<? extends GHPublicKey> clazz() {
            return GHRepositoryPublicKey.class;
        }

        @Override
        GHPublicKey wrap(GHPublicKey key) {
            return ((GHRepositoryPublicKey) key).wrap(repository);
        }
    }

    private static class OrgContext extends Context {
        private final GHOrganization organization;

        private OrgContext(GHOrganization organization) {
            super(organization.root());
            this.organization = organization;
        }

        @Override
        String collection() {
            return String.format("/orgs/%s/actions/secrets/public-key", organization.getLogin());
        }

        @Override
        Class<? extends GHPublicKey[]> collectionClass() {
            return GHOrgPublicKey[].class;
        }

        @Override
        Class<? extends GHPublicKey> clazz() {
            return GHOrgPublicKey.class;
        }

        @Override
        GHPublicKey wrap(GHPublicKey key) {
            return ((GHOrgPublicKey) key).wrap(organization);
        }
    }

    static Context repoContext(GHRepository repository, GHUser owner) {
        return new RepoContext(repository, owner);
    }

    static Context orgContext(GHOrganization organization) {
        return new OrgContext(organization);
    }
}
