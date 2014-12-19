package org.kohsuke.github;

import java.io.IOException;

public class GHDeploymentBuilder {
    private final GHRepository repo;
    private final Requester builder;

    public GHDeploymentBuilder(GHRepository repo) {
        this.repo = repo;
        this.builder = new Requester(repo.root);
    }

    public GHDeploymentBuilder ref(String branch) {
        builder.with("ref",branch);
        return this;
    }

    public GHDeploymentBuilder payload(String payload) {
        builder.with("payload",payload);
        return this;
    }

    public GHDeploymentBuilder description(String description) {
        builder.with("description",description);
        return this;
    }

    public GHDeployment create() throws IOException {
        return builder.to(repo.getApiTailUrl("deployments"),GHDeployment.class).wrap(repo);
    }
}
