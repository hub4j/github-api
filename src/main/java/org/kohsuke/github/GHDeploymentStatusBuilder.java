package org.kohsuke.github;

import java.io.IOException;

/**
 * Creates a new deployment status.
 *
 * @see GHDeployment#createStatus(GHDeploymentState) GHDeployment#createStatus(GHDeploymentState)
 */
public class GHDeploymentStatusBuilder {
    private final Requester builder;
    private GHRepository repo;
    private long deploymentId;

    /**
     * Instantiates a new Gh deployment status builder.
     *
     * @param repo
     *            the repo
     * @param deploymentId
     *            the deployment id
     * @param state
     *            the state
     * @deprecated Use {@link GHDeployment#createStatus(GHDeploymentState)}
     */
    public GHDeploymentStatusBuilder(GHRepository repo, int deploymentId, GHDeploymentState state) {
        this(repo, (long) deploymentId, state);
    }

    GHDeploymentStatusBuilder(GHRepository repo, long deploymentId, GHDeploymentState state) {
        this.repo = repo;
        this.deploymentId = deploymentId;
        this.builder = repo.root.retrieve().method("POST");
        this.builder.with("state", state);
    }

    /**
     * Description gh deployment status builder.
     *
     * @param description
     *            the description
     * @return the gh deployment status builder
     */
    public GHDeploymentStatusBuilder description(String description) {
        this.builder.with("description", description);
        return this;
    }

    /**
     * Target url gh deployment status builder.
     *
     * @param targetUrl
     *            the target url
     * @return the gh deployment status builder
     */
    public GHDeploymentStatusBuilder targetUrl(String targetUrl) {
        this.builder.with("target_url", targetUrl);
        return this;
    }

    /**
     * Create gh deployment status.
     *
     * @return the gh deployment status
     * @throws IOException
     *             the io exception
     */
    public GHDeploymentStatus create() throws IOException {
        return builder.withUrlPath(repo.getApiTailUrl("deployments/" + deploymentId + "/statuses"))
                .to(GHDeploymentStatus.class)
                .wrap(repo);
    }
}
