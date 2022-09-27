package org.kohsuke.github;

import org.kohsuke.github.internal.Previews;

import java.io.IOException;

// TODO: Auto-generated Javadoc
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
     *
     * @deprecated Use {@link GHDeployment#createStatus(GHDeploymentState)}
     */
    @Deprecated
    public GHDeploymentStatusBuilder(GHRepository repo, int deploymentId, GHDeploymentState state) {
        this(repo, (long) deploymentId, state);
    }

    /**
     * Instantiates a new GH deployment status builder.
     *
     * @param repo the repo
     * @param deploymentId the deployment id
     * @param state the state
     */
    GHDeploymentStatusBuilder(GHRepository repo, long deploymentId, GHDeploymentState state) {
        this.repo = repo;
        this.deploymentId = deploymentId;
        this.builder = repo.root()
                .createRequest()
                .withPreview(Previews.ANT_MAN)
                .withPreview(Previews.FLASH)
                .method("POST");

        this.builder.with("state", state);
    }

    /**
     * Add an inactive status to all prior non-transient, non-production environment deployments with the same
     * repository and environment name as the created status's deployment.
     *
     * @param autoInactive            Add inactive status flag
     * @return the gh deployment status builder
     * @deprecated until preview feature has graduated to stable
     */
    @Preview({ Previews.ANT_MAN, Previews.FLASH })
    public GHDeploymentStatusBuilder autoInactive(boolean autoInactive) {
        this.builder.with("auto_inactive", autoInactive);
        return this;
    }

    /**
     * Description gh deployment status builder.
     *
     * @param description
     *            the description
     *
     * @return the gh deployment status builder
     */
    public GHDeploymentStatusBuilder description(String description) {
        this.builder.with("description", description);
        return this;
    }

    /**
     * Name for the target deployment environment, which can be changed when setting a deploy status.
     *
     * @param environment            the environment name
     * @return the gh deployment status builder
     * @deprecated until preview feature has graduated to stable
     */
    @Preview(Previews.FLASH)
    public GHDeploymentStatusBuilder environment(String environment) {
        this.builder.with("environment", environment);
        return this;
    }

    /**
     * The URL for accessing the environment.
     *
     * @param environmentUrl            the environment url
     * @return the gh deployment status builder
     * @deprecated until preview feature has graduated to stable
     */
    @Preview(Previews.ANT_MAN)
    public GHDeploymentStatusBuilder environmentUrl(String environmentUrl) {
        this.builder.with("environment_url", environmentUrl);
        return this;
    }

    /**
     * The full URL of the deployment's output.
     * <p>
     * This method replaces {@link #targetUrl(String) targetUrl}.
     *
     * @param logUrl            the deployment output url
     * @return the gh deployment status builder
     * @deprecated until preview feature has graduated to stable
     */
    @Preview(Previews.ANT_MAN)
    public GHDeploymentStatusBuilder logUrl(String logUrl) {
        this.builder.with("log_url", logUrl);
        return this;
    }

    /**
     * Target url gh deployment status builder.
     *
     * @param targetUrl            the target url
     * @return the gh deployment status builder
     * @deprecated Target url is deprecated in favor of {@link #logUrl(String) logUrl}
     */
    @Deprecated
    public GHDeploymentStatusBuilder targetUrl(String targetUrl) {
        this.builder.with("target_url", targetUrl);
        return this;
    }

    /**
     * Create gh deployment status.
     *
     * @return the gh deployment status
     *
     * @throws IOException
     *             the io exception
     */
    public GHDeploymentStatus create() throws IOException {
        return builder.withUrlPath(repo.getApiTailUrl("deployments/" + deploymentId + "/statuses"))
                .fetch(GHDeploymentStatus.class)
                .lateBind(repo);
    }
}
