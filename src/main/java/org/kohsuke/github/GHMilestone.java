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
     */
    public void close() throws IOException {
        edit("state", "closed");
    }

    /**
     * Reopens this milestone.
     */
    public void reopen() throws IOException {
        edit("state", "open");
    }

    /**
     * Deletes this milestone.
     */
    public void delete() throws IOException {
        root.retrieve().method("DELETE").to(getApiRoute());
    }

    private void edit(String key, Object value) throws IOException {
        new Requester(root)._with(key, value).method("PATCH").to(getApiRoute());
    }

    public void setTitle(String title) throws IOException {
        edit("title", title);
    }

    public void setDescription(String description) throws IOException {
        edit("description", description);
    }

    public void setDueOn(Date dueOn) throws IOException {
        edit("due_on", GitHub.printDate(dueOn));
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
