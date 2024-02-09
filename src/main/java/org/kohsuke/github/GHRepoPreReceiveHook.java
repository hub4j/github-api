package org.kohsuke.github;

import java.io.IOException;
import java.net.URL;

public class GHRepoPreReceiveHook extends GHPreReceiveHook {

    transient GHRepository repository;

    public GHRepoPreReceiveHook() {
    }

    public GHRepoPreReceiveHook(long id, GHPreReceiveHookEnforcement enforcement) {
        this.id = id;
        this.enforcement = enforcement.toParameterValue();
    }

    @Override
    public URL getHtmlUrl() throws IOException {
        return null;
    }

    GHRepoPreReceiveHook wrap(GHRepository owner) {
        this.repository = owner;
        return this;
    }

}
