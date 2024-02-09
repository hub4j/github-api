package org.kohsuke.github;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GHPreReceiveHooks {

    static abstract class Context extends GitHubInteractiveObject {

        private Context(GitHub root) {
            super(root);
        }

        /**
         * Collection.
         *
         * @return the string
         */
        abstract String collection();

        /**
         * Collection class.
         *
         * @return the class<? extends GH pre-receive hook[]>
         */
        abstract Class<? extends GHPreReceiveHook[]> collectionClass();

        /**
         * Clazz.
         *
         * @return the class<? extends GH pre-receive hook>
         */
        abstract Class<? extends GHPreReceiveHook> clazz();

        /**
         * Wrap.
         *
         * @param hook
         *            the hook
         * @return the GH hook
         */
        abstract GHPreReceiveHook wrap(GHPreReceiveHook hook);

        abstract Map<String, Object> hookExtraParameters(GHPreReceiveHook hook);

        public GHPreReceiveHook configurePreReceiveHook(GHPreReceiveHook hook) throws IOException {
            GHPreReceiveHook updatedHook = root().createRequest()
                    .method("PATCH")
                    .with("enforcement", hook.getEnforcement())
                    .with(hookExtraParameters(hook))
                    .withUrlPath(collection(), Long.toString(hook.getId()))
                    .fetch(clazz());

            return wrap(updatedHook);
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
            return String.format("/orgs/%s/pre-receive-hooks", organization.login);
        }

        @Override
        Class<? extends GHPreReceiveHook[]> collectionClass() {
            return GHOrgPreReceiveHook[].class;
        }

        @Override
        Class<? extends GHPreReceiveHook> clazz() {
            return GHOrgPreReceiveHook.class;
        }

        @Override
        GHPreReceiveHook wrap(GHPreReceiveHook hook) {
            return ((GHOrgPreReceiveHook) hook).wrap(organization);
        }

        @Override
        Map<String, Object> hookExtraParameters(GHPreReceiveHook hook) {
            final Map<String, Object> params = new HashMap<>();
            if (hook instanceof GHOrgPreReceiveHook) {
                params.put("allow_downstream_configuration",
                        Boolean.valueOf(((GHOrgPreReceiveHook) hook).allowDownstreamConfiguration));
            }
            return params;
        }
    }

    private static class RepositoryContext extends Context {
        private final GHRepository repository;
        private final GHUser owner;

        private RepositoryContext(GHRepository repository, GHUser owner) {
            super(repository.root());
            this.repository = repository;
            this.owner = owner;
        }

        String collection() {
            return String.format("/repos/%s/%s/pre-receive-hooks", owner.getLogin(), repository.getName());
        }

        @Override
        Class<? extends GHPreReceiveHook[]> collectionClass() {
            return GHRepoPreReceiveHook[].class;
        }

        @Override
        Class<? extends GHPreReceiveHook> clazz() {
            return GHRepoPreReceiveHook.class;
        }

        @Override
        GHPreReceiveHook wrap(GHPreReceiveHook hook) {
            return ((GHRepoPreReceiveHook) hook).wrap(repository);
        }

        @Override
        Map<String, Object> hookExtraParameters(GHPreReceiveHook hook) {
            return Collections.emptyMap();
        }
    }

    static Context repoContext(GHRepository repository, GHUser owner) {
        return new RepositoryContext(repository, owner);
    }

    static Context orgContext(GHOrganization organization) {
        return new OrgContext(organization);
    }
}
