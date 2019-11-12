package org.kohsuke.github;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
public class GHIssueBuilder {
    private final GHRepository repo;
    private final Requester builder;
    private List<String> labels = new ArrayList<String>();
    private List<String> assignees = new ArrayList<String>();

    GHIssueBuilder(GHRepository repo, String title) {
        this.repo = repo;
        this.builder = repo.getRoot().createRequester();
        builder.with("title",title);
    }

    /**
     * Sets the main text of an issue, which is arbitrary multi-line text.
     */
    public GHIssueBuilder body(String str) {
        builder.with("body",str);
        return this;
    }

    public GHIssueBuilder assignee(GHUser user) {
        if (user!=null)
            assignees.add(user.getLogin());
        return this;
    }

    public GHIssueBuilder assignee(String user) {
        if (user!=null)
            assignees.add(user);
        return this;
    }

    public GHIssueBuilder milestone(GHMilestone milestone) {
        if (milestone!=null)
            builder.with("milestone",milestone.getNumber());
        return this;
    }

    public GHIssueBuilder label(String label) {
        if (label!=null)
            labels.add(label);
        return this;
    }

    /**
     * Creates a new issue.
     */
    public GHIssue create() throws IOException {
        return builder.with("labels",labels).with("assignees",assignees).to(repo.getApiTailUrl("issues"),GHIssue.class).wrap(repo);
    }
}
