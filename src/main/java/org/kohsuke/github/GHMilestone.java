package org.kohsuke.github;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Locale;

/**
 * The type GHMilestone.
 *
 * @author Yusuke Kokubo
 */
public class GHMilestone extends GHObject {
    GitHub root;
    GHRepository owner;

    GHUser creator;
    private String state, due_on, title, description, html_url;
    private int closed_issues, open_issues, number;
    protected String closed_at;

    /**
     * Gets root.
     *
     * @return the root
     */
    public GitHub getRoot() {
        return root;
    }

    /**
     * Gets owner.
     *
     * @return the owner
     */
    public GHRepository getOwner() {
        return owner;
    }

    /**
     * Gets creator.
     *
     * @return the creator
     * @throws IOException
     *             the io exception
     */
    public GHUser getCreator() throws IOException {
        return root.intern(creator);
    }

    /**
     * Gets due on.
     *
     * @return the due on
     */
    public Date getDueOn() {
        if (due_on == null)
            return null;
        return GitHub.parseDate(due_on);
    }

    /**
     * When was this milestone closed?
     *
     * @return the closed at
     * @throws IOException
     *             the io exception
     */
    public Date getClosedAt() throws IOException {
        return GitHub.parseDate(closed_at);
    }

    /**
     * Gets title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets closed issues.
     *
     * @return the closed issues
     */
    public int getClosedIssues() {
        return closed_issues;
    }

    /**
     * Gets open issues.
     *
     * @return the open issues
     */
    public int getOpenIssues() {
        return open_issues;
    }

    /**
     * Gets number.
     *
     * @return the number
     */
    public int getNumber() {
        return number;
    }

    public URL getHtmlUrl() {
        return GitHub.parseURL(html_url);
    }

    /**
     * Gets state.
     *
     * @return the state
     */
    public GHMilestoneState getState() {
        return Enum.valueOf(GHMilestoneState.class, state.toUpperCase(Locale.ENGLISH));
    }

    /**
     * Closes this milestone.
     *
     * @throws IOException
     *             the io exception
     */
    public void close() throws IOException {
        edit("state", "closed");
    }

    /**
     * Reopens this milestone.
     *
     * @throws IOException
     *             the io exception
     */
    public void reopen() throws IOException {
        edit("state", "open");
    }

    /**
     * Deletes this milestone.
     *
     * @throws IOException
     *             the io exception
     */
    public void delete() throws IOException {
        root.retrieve().method("DELETE").to(getApiRoute());
    }

    private void edit(String key, Object value) throws IOException {
        root.retrieve().method("POST").with(key, value).method("PATCH").to(getApiRoute());
    }

    /**
     * Sets title.
     *
     * @param title
     *            the title
     * @throws IOException
     *             the io exception
     */
    public void setTitle(String title) throws IOException {
        edit("title", title);
    }

    /**
     * Sets description.
     *
     * @param description
     *            the description
     * @throws IOException
     *             the io exception
     */
    public void setDescription(String description) throws IOException {
        edit("description", description);
    }

    /**
     * Sets due on.
     *
     * @param dueOn
     *            the due on
     * @throws IOException
     *             the io exception
     */
    public void setDueOn(Date dueOn) throws IOException {
        edit("due_on", GitHub.printDate(dueOn));
    }

    /**
     * Gets api route.
     *
     * @return the api route
     */
    protected String getApiRoute() {
        return "/repos/" + owner.getOwnerName() + "/" + owner.getName() + "/milestones/" + number;
    }

    /**
     * Wrap gh milestone.
     *
     * @param repo
     *            the repo
     * @return the gh milestone
     */
    public GHMilestone wrap(GHRepository repo) {
        this.owner = repo;
        this.root = repo.root;
        return this;
    }
}
