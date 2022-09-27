package org.kohsuke.github;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * Utility class for creating and retrieving webhooks; removes duplication between GHOrganization and GHRepository
 * functionality.
 */
class GHHooks {

    /**
     * The Class Context.
     */
    static abstract class Context extends GitHubInteractiveObject {

        private Context(GitHub root) {
            super(root);
        }

        /**
         * Gets hooks.
         *
         * @return the hooks
         * @throws IOException
         *             the io exception
         */
        public List<GHHook> getHooks() throws IOException {

            // jdk/eclipse bug
            GHHook[] hookArray = root().createRequest().withUrlPath(collection()).fetch(collectionClass());
            // requires this
            // to be on separate line
            List<GHHook> list = new ArrayList<GHHook>(Arrays.asList(hookArray));
            for (GHHook h : list)
                wrap(h);
            return list;
        }

        /**
         * Gets hook.
         *
         * @param id
         *            the id
         * @return the hook
         * @throws IOException
         *             the io exception
         */
        public GHHook getHook(int id) throws IOException {
            GHHook hook = root().createRequest().withUrlPath(collection() + "/" + id).fetch(clazz());
            return wrap(hook);
        }

        /**
         * Create hook gh hook.
         *
         * @param name
         *            the name
         * @param config
         *            the config
         * @param events
         *            the events
         * @param active
         *            the active
         * @return the gh hook
         * @throws IOException
         *             the io exception
         */
        public GHHook createHook(String name, Map<String, String> config, Collection<GHEvent> events, boolean active)
                throws IOException {
            List<String> ea = null;
            if (events != null) {
                ea = new ArrayList<String>();
                for (GHEvent e : events)
                    ea.add(e.symbol());
            }

            GHHook hook = root().createRequest()
                    .method("POST")
                    .with("name", name)
                    .with("active", active)
                    .with("config", config)
                    .with("events", ea)
                    .withUrlPath(collection())
                    .fetch(clazz());

            return wrap(hook);
        }

        /**
         * Deletes hook.
         *
         * @param id
         *            the id
         * @throws IOException
         *             the io exception
         */
        public void deleteHook(int id) throws IOException {
            root().createRequest().method("DELETE").withUrlPath(collection() + "/" + id).send();
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
         * @return the class<? extends GH hook[]>
         */
        abstract Class<? extends GHHook[]> collectionClass();

        /**
         * Clazz.
         *
         * @return the class<? extends GH hook>
         */
        abstract Class<? extends GHHook> clazz();

        /**
         * Wrap.
         *
         * @param hook
         *            the hook
         * @return the GH hook
         */
        abstract GHHook wrap(GHHook hook);
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
            return String.format("/repos/%s/%s/hooks", owner.getLogin(), repository.getName());
        }

        @Override
        Class<? extends GHHook[]> collectionClass() {
            return GHRepoHook[].class;
        }

        @Override
        Class<? extends GHHook> clazz() {
            return GHRepoHook.class;
        }

        @Override
        GHHook wrap(GHHook hook) {
            return ((GHRepoHook) hook).wrap(repository);
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
            return String.format("/orgs/%s/hooks", organization.getLogin());
        }

        @Override
        Class<? extends GHHook[]> collectionClass() {
            return GHOrgHook[].class;
        }

        @Override
        Class<? extends GHHook> clazz() {
            return GHOrgHook.class;
        }

        @Override
        GHHook wrap(GHHook hook) {
            return ((GHOrgHook) hook).wrap(organization);
        }
    }

    /**
     * Repo context.
     *
     * @param repository
     *            the repository
     * @param owner
     *            the owner
     * @return the context
     */
    static Context repoContext(GHRepository repository, GHUser owner) {
        return new RepoContext(repository, owner);
    }

    /**
     * Org context.
     *
     * @param organization
     *            the organization
     * @return the context
     */
    static Context orgContext(GHOrganization organization) {
        return new OrgContext(organization);
    }
}
