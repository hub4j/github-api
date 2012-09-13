package org.kohsuke.github;

import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 */
public class GHIssueBuilder {
    private final GHRepository repo;
    private final Requester builder;

    GHIssueBuilder(GHRepository repo, String title) {
        this.repo = repo;
        this.builder = new Requester(repo.root);
        builder.with("title",title);
    }

    public GHIssueBuilder body(String str) {
        builder.with("body",str);
        return this;
    }

    /**
     * Creates a new issue.
     */
    public GHIssue create() throws IOException {
        return builder.to("/repos/"+repo.getOwnerName()+'/'+repo.getName()+"/issues",GHIssue.class).wrap(repo);
    }
}
