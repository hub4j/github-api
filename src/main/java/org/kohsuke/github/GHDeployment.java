package org.kohsuke.github;

import java.net.URL;
import java.util.Date;

public class GHDeployment {
    private GHRepository owner;
    private GitHub root;
    protected String url;
    protected String sha;
    protected int id;
    protected String task;
    protected String payload;
    protected String environment;
    protected String description;
    protected String statuses_url;
    protected String repository_url;
    protected String created_at;
    protected String updated_at;
    protected GHUser creator;


    GHDeployment wrap(GHRepository owner) {
        this.owner = owner;
        this.root = owner.root;
        if(creator != null) creator.wrapUp(root);
        return this;
    }
    public Date getCreatedAt() {
        return GitHub.parseDate(created_at);
    }

    public URL getUrl() {
        return GitHub.parseURL(url);
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

    public Date getUpdatedAt() {
        return GitHub.parseDate(updated_at);
    }

    public int getId() {
        return id;
    }
}
