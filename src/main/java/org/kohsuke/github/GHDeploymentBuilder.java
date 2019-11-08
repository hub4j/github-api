package org.kohsuke.github;

import java.io.IOException;
import java.util.List;

//Based on https://developer.github.com/v3/repos/deployments/#create-a-deployment
public class GHDeploymentBuilder {
    private final GHRepository repo;
    private final Requester builder;

    public GHDeploymentBuilder(GHRepository repo) {
        this.repo = repo;
        this.builder = repo.root.createRequester();
    }

    public GHDeploymentBuilder(GHRepository repo, String ref) {
        this(repo);
        ref(ref);
    }

    public GHDeploymentBuilder ref(String branch) {
        builder.with("ref",branch);
        return this;
    }
    public GHDeploymentBuilder task(String task) {
        builder.with("task",task);
        return this;
    }
    public GHDeploymentBuilder autoMerge(boolean autoMerge) {
        builder.with("auto_merge",autoMerge);
        return this;
    }

    public GHDeploymentBuilder requiredContexts(List<String> requiredContexts) {
        builder.with("required_contexts",requiredContexts);
        return this;
    }
    public GHDeploymentBuilder payload(String payload) {
        builder.with("payload",payload);
        return this;
    }

    public GHDeploymentBuilder environment(String environment) {
        builder.with("environment",environment);
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
