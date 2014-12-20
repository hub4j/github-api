package org.kohsuke.github;

import java.net.URL;

public class GHDeploymentStatus extends Identifiable {
    private GHRepository owner;
    private GitHub root;
    protected GHUser creator;
    protected String state;
    protected String description;
    protected String target_url;
    protected String deployment_url;
    protected String repository_url;
    public GHDeploymentStatus wrap(GHRepository owner) {
        this.owner = owner;
        this.root = owner.root;
        if(creator != null) creator.wrapUp(root);
        return this;
    }
    public URL getTargetUrl() {
        return GitHub.parseURL(target_url);
    }

    public URL getDeploymentUrl() {
        return GitHub.parseURL(deployment_url);
    }

    public URL getRepositoryUrl() {
        return GitHub.parseURL(repository_url);
    }
    public GHCommitState getState() {
        return GHCommitState.valueOf(state.toUpperCase());
    }


}
