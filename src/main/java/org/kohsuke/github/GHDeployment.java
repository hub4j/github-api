package org.kohsuke.github;

import com.fasterxml.jackson.annotation.JacksonInject;

import java.io.IOException;
import java.net.URL;

/**
 * Represents a deployment
 *
 * @see <a href="https://developer.github.com/v3/repos/deployments/">documentation</a>
 * @see GHRepository#listDeployments(String, String, String, String)
 * @see GHRepository#getDeployment(long)
 */
public class GHDeployment extends GHObject {

    @JacksonInject(value = "org.kohsuke.github.GHRepository")
    GHRepository owner;

    protected String sha;
    protected String ref;
    protected String task;
    protected Object payload;
    protected String environment;
    protected String description;
    protected String statuses_url;
    protected String repository_url;
    protected GHUser creator;

    public URL getStatusesUrl() {
        return GitHub.parseURL(statuses_url);
    }

    public URL getRepositoryUrl() {
        return GitHub.parseURL(repository_url);
    }

    public String getTask() {
        return task;
    }
    public String getPayload() {
        return (String) payload;
    }
    public String getEnvironment() {
        return environment;
    }
    public GHUser getCreator() throws IOException {
        return getRoot().intern(creator);
    }
    public String getRef() {
        return ref;
    }
    public String getSha(){
        return sha;
    }

    /**
     * @deprecated This object has no HTML URL.
     */
    @Override
    public URL getHtmlUrl() {
        return null;
    }

    public GHDeploymentStatusBuilder createStatus(GHDeploymentState state) {
        return new GHDeploymentStatusBuilder(owner,id,state);
    }

    public PagedIterable<GHDeploymentStatus> listStatuses() {
        return getRoot().createRequest().method("GET")
            .asPagedIterable(
                statuses_url,
                GHDeploymentStatus[].class,
                item -> item.wrap(owner) );
    }

}
