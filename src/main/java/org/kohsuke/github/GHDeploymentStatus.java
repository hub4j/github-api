package org.kohsuke.github;

import org.kohsuke.github.internal.Previews;

import java.net.URL;
import java.util.Locale;

/**
 * The type GHDeploymentStatus.
 */
public class GHDeploymentStatus extends GHObject {
    private GHRepository owner;
    protected GHUser creator;
    protected String state;
    protected String description;
    protected String target_url;
    protected String log_url;
    protected String deployment_url;
    protected String repository_url;
    protected String environment_url;

    /**
     * Wrap gh deployment status.
     *
     * @param owner
     *            the owner
     *
     * @return the gh deployment status
     */
    @Deprecated
    public GHDeploymentStatus wrap(GHRepository owner) {
        throw new RuntimeException("Do not use this method.");
    }

    /**
     * Wrap gh deployment status.
     *
     * @param owner
     *            the owner
     *
     * @return the gh deployment status
     */
    GHDeploymentStatus lateBind(GHRepository owner) {
        this.owner = owner;
        return this;
    }

    /**
     * Gets target url.
     *
     * @deprecated Target url is deprecated in favor of {@link #getLogUrl() getLogUrl}
     *
     * @return the target url
     */
    @Deprecated
    public URL getTargetUrl() {
        return GitHubClient.parseURL(target_url);
    }

    /**
     * Gets target url.
     * <p>
     * This method replaces {@link #getTargetUrl() getTargetUrl}}.
     *
     * @deprecated until preview feature has graduated to stable
     *
     * @return the target url
     */
    @Preview(Previews.ANT_MAN)
    public URL getLogUrl() {
        return GitHubClient.parseURL(log_url);
    }

    /**
     * Gets deployment url.
     *
     * @return the deployment url
     */
    public URL getDeploymentUrl() {
        return GitHubClient.parseURL(deployment_url);
    }

    /**
     * Gets deployment environment url.
     *
     * @deprecated until preview feature has graduated to stable
     *
     * @return the deployment environment url
     */
    @Preview(Previews.ANT_MAN)
    public URL getEnvironmentUrl() {
        return GitHubClient.parseURL(environment_url);
    }

    /**
     * Gets repository url.
     *
     * @return the repository url
     */
    public URL getRepositoryUrl() {
        return GitHubClient.parseURL(repository_url);
    }

    /**
     * Gets state.
     *
     * @return the state
     */
    public GHDeploymentState getState() {
        return GHDeploymentState.valueOf(state.toUpperCase(Locale.ENGLISH));
    }

    /**
     * @deprecated This object has no HTML URL.
     */
    @Override
    public URL getHtmlUrl() {
        return null;
    }

    // test only
    GHRepository getOwner() {
        return owner;
    }
}
