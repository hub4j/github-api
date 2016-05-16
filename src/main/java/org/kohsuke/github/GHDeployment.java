package org.kohsuke.github;

import java.io.IOException;
import java.net.URL;

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
        if(creator != null) creator.wrapUp(root);
        return this;
    }

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
        if(creator != null) return root.getUser(creator.getLogin());
        return creator;
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
}
