package org.kohsuke.github;

import java.io.IOException;
import java.net.URL;

/**
 * Represents a deployment
 *
 * @see <a href="https://developer.github.com/v3/repos/deployments/">documentation</a>
 * @see GHRepository#listDeployments(String, String, String, String) GHRepository#listDeployments(String, String,
 *      String, String)
 * @see GHRepository#getDeployment(long) GHRepository#getDeployment(long)
 */
public class GHDeployment extends GHObject {
    private GHRepository owner;
    private GitHub root;
    protected String sha;
    protected String ref;
    protected String task;
    protected Object payload;
    protected String environment;
    protected String description;
    protected String statuses_url;
    protected String repository_url;
    protected GHUser creator;

    GHDeployment wrap(GHRepository owner) {
        this.owner = owner;
        this.root = owner.root;
        if (creator != null)
            creator.wrapUp(root);
        return this;
    }

    /**
     * Gets statuses url.
     *
     * @return the statuses url
     */
    public URL getStatusesUrl() {
        return GitHub.parseURL(statuses_url);
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
     * Gets task.
     *
     * @return the task
     */
    public String getTask() {
        return task;
    }

    /**
     * Gets payload.
     *
     * @return the payload
     */
    public String getPayload() {
        return (String) payload;
    }

    /**
     * Gets environment.
     *
     * @return the environment
     */
    public String getEnvironment() {
        return environment;
    }

    /**
     * Gets creator.
     *
     * @return the creator
     * @throws IOException
     *             the io exception
     */
    public GHUser getCreator() throws IOException {
        return root.intern(creator);
    }

    /**
     * Gets ref.
     *
     * @return the ref
     */
    public String getRef() {
        return ref;
    }

    /**
     * Gets sha.
     *
     * @return the sha
     */
    public String getSha() {
        return sha;
    }

    /**
     * @deprecated This object has no HTML URL.
     */
    @Override
    public URL getHtmlUrl() {
        return null;
    }

    /**
     * Create status gh deployment status builder.
     *
     * @param state
     *            the state
     * @return the gh deployment status builder
     */
    public GHDeploymentStatusBuilder createStatus(GHDeploymentState state) {
        return new GHDeploymentStatusBuilder(owner, id, state);
    }

    /**
     * List statuses paged iterable.
     *
     * @return the paged iterable
     */
    public PagedIterable<GHDeploymentStatus> listStatuses() {
        return root.retrieve().asPagedIterable(statuses_url, GHDeploymentStatus[].class, item -> item.wrap(owner));
    }

}
