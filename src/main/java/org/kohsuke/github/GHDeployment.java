package org.kohsuke.github;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

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
    protected String sha;
    protected String ref;
    protected String task;
    protected Object payload;
    protected String environment;
    protected String description;
    protected String statuses_url;
    protected String repository_url;
    protected GHUser creator;
    protected String original_environment;
    protected boolean transient_environment;
    protected boolean production_environment;

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
        return GitHubClient.parseURL(statuses_url);
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
     * Gets task.
     *
     * @return the task
     */
    public String getTask() {
        return task;
    }

    /**
     * Gets payload. <b>NOTE:</b> only use this method if you can guarantee the payload will be a simple string,
     * otherwise use {@link #getPayloadObject()}.
     *
     * @return the payload
     */
    public String getPayload() {
        return (String) payload;
    }

    /**
     * Gets payload. <b>NOTE:</b> only use this method if you can guarantee the payload will be a JSON object (Map),
     * otherwise use {@link #getPayloadObject()}.
     *
     * @return the payload
     */
    public Map<String, Object> getPayloadMap() {
        return (Map<String, Object>) payload;
    }

    /**
     * Gets payload without assuming its type. It could be a String or a Map.
     *
     * @return the payload
     */
    public Object getPayloadObject() {
        return payload;
    }

    /**
     * The environment defined when the deployment was first created.
     *
     * @deprecated until preview feature has graduated to stable
     *
     * @return the original deployment environment
     */
    @Deprecated
    @Preview(Previews.FLASH)
    public String getOriginalEnvironment() {
        return original_environment;
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
     * Specifies if the given environment is specific to the deployment and will no longer exist at some point in the
     * future.
     *
     * @deprecated until preview feature has graduated to stable
     *
     * @return the environment is transient
     */
    @Deprecated
    @Preview(Previews.ANT_MAN)
    public boolean isTransientEnvironment() {
        return transient_environment;
    }

    /**
     * Specifies if the given environment is one that end-users directly interact with.
     *
     * @deprecated until preview feature has graduated to stable
     *
     * @return the environment is used by end-users directly
     */
    @Deprecated
    @Preview(Previews.ANT_MAN)
    public boolean isProductionEnvironment() {
        return production_environment;
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
        return new GHDeploymentStatusBuilder(owner, getId(), state);
    }

    /**
     * List statuses paged iterable.
     *
     * @return the paged iterable
     */
    public PagedIterable<GHDeploymentStatus> listStatuses() {
        return root.createRequest()
                .withUrlPath(statuses_url)
                .withPreview(Previews.ANT_MAN)
                .withPreview(Previews.FLASH)
                .toIterable(GHDeploymentStatus[].class, item -> item.wrap(owner));
    }

}
