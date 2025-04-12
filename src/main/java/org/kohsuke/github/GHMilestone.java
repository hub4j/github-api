package org.kohsuke.github;

import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;

// TODO: Auto-generated Javadoc
/**
 * The type GHMilestone.
 *
 * @author Yusuke Kokubo
 */
public class GHMilestone extends GHObject {

    /**
     * Create default GHMilestone instance
     */
    public GHMilestone() {
    }

    /** The owner. */
    GHRepository owner;

    /** The creator. */
    GHUser creator;
    private String state, dueOn, title, description, htmlUrl;
    private int closedIssues, openIssues, number;

    /** The closed at. */
    protected String closedAt;

    /**
     * Gets owner.
     *
     * @return the owner
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHRepository getOwner() {
        return owner;
    }

    /**
     * Gets creator.
     *
     * @return the creator
     */
    public GHUser getCreator() {
        return root().intern(creator);
    }

    /**
     * Gets due on.
     *
     * @return the due on
     */
    @WithBridgeMethods(value = Date.class, adapterMethod = "instantToDate")
    public Instant getDueOn() {
        return GitHubClient.parseInstant(dueOn);
    }

    /**
     * When was this milestone closed?.
     *
     * @return the closed at
     */
    @WithBridgeMethods(value = Date.class, adapterMethod = "instantToDate")
    public Instant getClosedAt() {
        return GitHubClient.parseInstant(closedAt);
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
        return closedIssues;
    }

    /**
     * Gets open issues.
     *
     * @return the open issues
     */
    public int getOpenIssues() {
        return openIssues;
    }

    /**
     * Gets number.
     *
     * @return the number
     */
    public int getNumber() {
        return number;
    }

    /**
     * Gets the html url.
     *
     * @return the html url
     */
    public URL getHtmlUrl() {
        return GitHubClient.parseURL(htmlUrl);
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
        root().createRequest().method("DELETE").withUrlPath(getApiRoute()).send();
    }

    private void edit(String key, Object value) throws IOException {
        root().createRequest().with(key, value).method("PATCH").withUrlPath(getApiRoute()).send();
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
     * @deprecated Use {@link #setDueOn(Instant)}
     */
    @Deprecated
    public void setDueOn(Date dueOn) throws IOException {
        setDueOn(GitHubClient.toInstantOrNull(dueOn));
    }

    /**
     * Sets due on.
     *
     * @param dueOn
     *            the due on
     * @throws IOException
     *             the io exception
     */
    public void setDueOn(Instant dueOn) throws IOException {
        edit("due_on", GitHubClient.printInstant(dueOn));
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
    GHMilestone lateBind(GHRepository repo) {
        this.owner = repo;
        return this;
    }

}
