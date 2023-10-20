package org.kohsuke.github;

import org.kohsuke.github.internal.Previews;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * Represents a deployment.
 *
 * @see <a href="https://developer.github.com/v3/repos/deployments/">documentation</a>
 * @see GHRepository#listDeployments(String, String, String, String) GHRepository#listDeployments(String, String,
 *      String, String)
 * @see GHRepository#getDeployment(long) GHRepository#getDeployment(long)
 */
public class GHDeployment extends GHObject {
    private GHRepository owner;

    /** The sha. */
    protected String sha;

    /** The ref. */
    protected String ref;

    /** The task. */
    protected String task;

    /** The payload. */
    protected Object payload;

    /** The environment. */
    protected String environment;

    /** The description. */
    protected String description;

    /** The statuses url. */
    protected String statuses_url;

    /** The repository url. */
    protected String repository_url;

    /** The creator. */
    protected GHUser creator;

    /** The original environment. */
    protected String original_environment;

    /** The transient environment. */
    protected boolean transient_environment;

    /** The production environment. */
    protected boolean production_environment;

    /**
     * Wrap.
     *
     * @param owner
     *            the owner
     * @return the GH deployment
     */
    GHDeployment wrap(GHRepository owner) {
        this.owner = owner;
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
        return Collections.unmodifiableMap((Map<String, Object>) payload);
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
     * @return the original deployment environment
     * @deprecated until preview feature has graduated to stable
     */
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
     * @return the environment is transient
     * @deprecated until preview feature has graduated to stable
     */
    @Preview(Previews.ANT_MAN)
    public boolean isTransientEnvironment() {
        return transient_environment;
    }

    /**
     * Specifies if the given environment is one that end-users directly interact with.
     *
     * @return the environment is used by end-users directly
     * @deprecated until preview feature has graduated to stable
     */
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
        return root().intern(creator);
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
        return root().createRequest()
                .withUrlPath(statuses_url)
                .withPreview(Previews.ANT_MAN)
                .withPreview(Previews.FLASH)
                .toIterable(GHDeploymentStatus[].class, item -> item.lateBind(owner));
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
