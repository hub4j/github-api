package org.kohsuke.github;

import java.io.IOException;
import java.net.URL;

public class GHOrgPreReceiveHook extends GHPreReceiveHook {

    transient GHOrganization organization;

    boolean allowDownstreamConfiguration;

    public GHOrgPreReceiveHook() {
    }

    public GHOrgPreReceiveHook(long id, GHPreReceiveHookEnforcement enforcement, boolean allowDownstreamConfiguration) {
        this.id = id;
        this.enforcement = enforcement.toParameterValue();
        this.allowDownstreamConfiguration = allowDownstreamConfiguration;
    }

    @Override
    public URL getHtmlUrl() throws IOException {
        return null;
    }

    GHOrgPreReceiveHook wrap(GHOrganization organization) {
        this.organization = organization;
        return this;
    }

    static GHOrgPreReceiveHook makeHook(long id,
            GHPreReceiveHookEnforcement enforcement,
            boolean allowDownstreamConfiguration) {
        final GHOrgPreReceiveHook hook = new GHOrgPreReceiveHook();
        hook.id = id;
        hook.enforcement = enforcement.toParameterValue();
        hook.allowDownstreamConfiguration = allowDownstreamConfiguration;
        return hook;
    }

}
