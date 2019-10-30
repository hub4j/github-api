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
public class GHMilestone extends GHObject {
    GitHub root;
    GHRepository owner;

    GHUser creator;
    private String state, due_on, title, description, html_url;
    private int closed_issues, open_issues, number;
    protected String closed_at;

    public GitHub getRoot() {
        return root;
    }
    
    public GHRepository getOwner() {
        return owner;
    }
    
    public GHUser getCreator() throws IOException {
        return root.intern(creator);
    }
    
    public Date getDueOn() {
        if (due_on == null) return null;
        return GitHub.parseDate(due_on);
    }

    /**
     * When was this milestone closed?
     */
    public Date getClosedAt() throws IOException {
        return GitHub.parseDate(closed_at);
    }

    public String getTitle() {
        return title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getClosedIssues() {
        return closed_issues;
    }
    
    public int getOpenIssues() {
        return open_issues;
    }
    
    public int getNumber() {
        return number;
    }

    public URL getHtmlUrl() {
        return GitHub.parseURL(html_url);
    }
    
    public GHMilestoneState getState() {
        return Enum.valueOf(GHMilestoneState.class, state.toUpperCase(Locale.ENGLISH));
    }

    /**
     * Closes this milestone.
     * @deprecated use {@link #update()} instead
     */
    @Deprecated
    public void close() throws IOException {
        update().close();
    }

    /**
     * Reopens this milestone.
     * @deprecated use {@link #update()} instead
     */
    @Deprecated
    public void reopen() throws IOException {
        update().reopen();
    }

    /**
     * Gets a GHMilestoneUpdater
     */
    public GHMilestoneUpdater update() throws IOException {
        return new GHMilestoneUpdater(this);
    }

    /**
     * Deletes this milestone.
     */
    public void delete() throws IOException {
        root.retrieve().method("DELETE").to(getApiRoute());
    }

    protected String getApiRoute() {
        return "/repos/"+owner.getOwnerName()+"/"+owner.getName()+"/milestones/"+number;
    }

    public GHMilestone wrap(GHRepository repo) {
        this.owner = repo;
        this.root = repo.root;
        return this;
    }
}
