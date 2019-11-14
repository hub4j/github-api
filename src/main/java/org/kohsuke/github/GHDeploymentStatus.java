package org.kohsuke.github;

import java.net.URL;
import java.util.Locale;

/**
 * The type GHDeploymentStatus.
 */
public class GHDeploymentStatus extends GHObject {
    private GHRepository owner;
    private GitHub root;
    protected GHUser creator;
    protected String state;
    protected String description;
    protected String target_url;
    protected String deployment_url;
    protected String repository_url;

    /**
     * Wrap gh deployment status.
     *
     * @param owner
     *            the owner
     * @return the gh deployment status
     */
    public GHDeploymentStatus wrap(GHRepository owner) {
        this.owner = owner;
        this.root = owner.root;
        if (creator != null)
            creator.wrapUp(root);
        return this;
    }

    /**
     * Gets target url.
     *
     * @return the target url
     */
    public URL getTargetUrl() {
        return GitHub.parseURL(target_url);
    }

    /**
     * Gets deployment url.
     *
     * @return the deployment url
     */
    public URL getDeploymentUrl() {
        return GitHub.parseURL(deployment_url);
    }

    /**
     * Gets repository url.
     *
     * @return the repository url
     */
    public URL getRepositoryUrl() {
        return GitHub.parseURL(repository_url);
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
}
