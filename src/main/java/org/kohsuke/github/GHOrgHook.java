/*
 * © Copyright 2015 -  SourceClear Inc
 */

package org.kohsuke.github;

class GHOrgHook extends GHHook {
    /**
     * Organization that the hook belongs to.
     */
    /*package*/ transient GHOrganization organization;

    /*package*/ GHOrgHook wrap(GHOrganization owner) {
        this.setRoot(owner.getRoot());
        this.organization = owner;
        return this;
    }

    @Override
    String getApiRoute() {
        return String.format("/orgs/%s/hooks/%d", organization.getLogin(), id);
    }
}
