package org.kohsuke.github;


import java.net.URL;
import java.util.Date;

public class GHDeployment extends Identifiable {
    private GHRepository owner;
    private GitHub root;
    protected String sha;
    protected String task;
    protected String payload;
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

    public GHUser getCreator() {
        return creator;
    }

}
