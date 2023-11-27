package org.kohsuke.github;

import org.kohsuke.github.internal.Previews;

import java.net.URL;
import java.util.Locale;

// TODO: Auto-generated Javadoc
/**
 * The type GHDeploymentStatus.
 */
public class GHDeploymentStatus extends GHObject {
    private GHRepository owner;

    /** The creator. */
    protected GHUser creator;

    /** The state. */
    protected String state;

    /** The description. */
    protected String description;

    /** The target url. */
    protected String target_url;

    /** The log url. */
    protected String log_url;

    /** The deployment url. */
    protected String deployment_url;

    /** The repository url. */
    protected String repository_url;

    /** The environment url. */
    protected String environment_url;

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
     * <p>
     * This method replaces {@link #getTargetUrl() getTargetUrl}}.
     *
     * @return the target url
     * @deprecated until preview feature has graduated to stable
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
     * @return the deployment environment url
     * @deprecated until preview feature has graduated to stable
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
     * Gets the html url.
     *
     * @return the html url
     * @deprecated This object has no HTML URL.
     */
    @Override
    public URL getHtmlUrl() {
        return null;
    }

    /**
     * Gets the owner.
     *
     * @return the owner
     */
    // test only
    GHRepository getOwner() {
        return owner;
    }
}
