package org.kohsuke.github;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Locale;

/**
 * 
 * @author Yusuke Kokubo
 *
 */
public class GHMilestoneUpdater {
    private final GHMilestone base;
    private final Requester builder;

    GHMilestoneUpdater(GHMilestone base) {
        this.base = base;
        this.builder = new Requester(base.root)
            .method("PATCH");
    }

    /**
     * Updates the Milestone based on the parameters specified and
     * closes this milestone.
     */
    public GHMilestone close() throws IOException {
        builder.with("state", "closed");
        return update();
    }

    /**
     * Updates the Milestone based on the parameters specified and
     * reopens this milestone.
     */
    public GHMilestone reopen() throws IOException {
        builder.with("state", "open");
        return update();
    }

    /**
     * Updates the Milestone based on the parameters specified thus far.
     */
    public GHMilestone update() throws IOException {
        return builder
            .to(getApiRoute(), GHMilestone.class).wrap(base.owner);
    }

    public GHMilestoneUpdater title(String title) {
        builder.with("title", title);
        return this;
    }

    public GHMilestoneUpdater description(String description) {
        builder.with("description", description);
        return this;
    }

    public GHMilestoneUpdater dueOn(Date dueOn) {
        builder.with("due_on", GitHub.printDate(dueOn));
        return this;
    }

    protected String getApiRoute() {
        return "/repos/"+base.owner.getOwnerName()+"/"+base.owner.getName()+"/milestones/"+base.getNumber();
    }
}
