/*
 * © Copyright 2015 -  SourceClear Inc
 */

package org.kohsuke.github;

// TODO: Auto-generated Javadoc
/**
 * The Class GHOrgHook.
 */
class GHOrgHook extends GHHook {
    /**
     * Organization that the hook belongs to.
     */
    transient GHOrganization organization;

    /**
     * Wrap.
     *
     * @param owner the owner
     * @return the GH org hook
     */
    GHOrgHook wrap(GHOrganization owner) {
        this.organization = owner;
        return this;
    }

    /**
     * Root.
     *
     * @return the git hub
     */
    @Override
    GitHub root() {
        return organization.root();
    }

    /**
     * Gets the api route.
     *
     * @return the api route
     */
    @Override
    String getApiRoute() {
        return String.format("/orgs/%s/hooks/%d", organization.getLogin(), getId());
    }
}
