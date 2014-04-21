package org.kohsuke.github;


import java.io.IOException;

public class GHRefBuilder {

    private final GHRepository repo;
    private final Requester builder;

    public GHRefBuilder(GHRepository ghRepository, String ref, String sha) {
        this.repo = ghRepository;
        this.builder = new Requester(repo.root);
        builder.with("ref", "refs/heads/" +ref);
        builder.with("sha", sha);
    }

    public void create() throws IOException {
         builder.method("POST").to(repo.getApiTailUrl("git/refs"));
    }

}
