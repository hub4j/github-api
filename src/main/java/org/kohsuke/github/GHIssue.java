/*
 * The MIT License
 *
 * Copyright (c) 2011, Eric Maupin, Kohsuke Kawaguchi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.kohsuke.github;

import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.kohsuke.github.Previews.SQUIRREL_GIRL;

/**
 * Represents an issue on GitHub.
 *
 * @author Eric Maupin
 * @author Kohsuke Kawaguchi
 * @see GHRepository#getIssue(int) GHRepository#getIssue(int)
 * @see GitHub#searchIssues() GitHub#searchIssues()
 * @see GHIssueSearchBuilder
 */
public class GHIssue extends GHObject implements Reactable {
    private static final String ASSIGNEES = "assignees";

    GitHub root;
    GHRepository owner;

    // API v3
    protected GHUser assignee; // not sure what this field is now that 'assignees' exist
    protected GHUser[] assignees;
    protected String state;
    protected int number;
    protected String closed_at;
    protected int comments;
    @SkipFromToString
    protected String body;
    // for backward compatibility with < 1.63, this collection needs to hold instances of Label, not GHLabel
    protected List<Label> labels;
    protected GHUser user;
    protected String title, html_url;
    protected GHIssue.PullRequest pull_request;
    protected GHMilestone milestone;
    protected GHUser closed_by;
    protected boolean locked;

    /**
     * The type Label.
     *
     * @deprecated use {@link GHLabel}
     */
    public static class Label extends GHLabel {
    }

    GHIssue wrap(GHRepository owner) {
        this.owner = owner;
        if (milestone != null)
            milestone.wrap(owner);
        return wrap(owner.root);
    }

    GHIssue wrap(GitHub root) {
        this.root = root;
        if (assignee != null)
            assignee.wrapUp(root);
        if (assignees != null)
            GHUser.wrap(assignees, root);
        if (user != null)
            user.wrapUp(root);
        if (closed_by != null)
            closed_by.wrapUp(root);
        return this;
    }

    static GHIssue[] wrap(GHIssue[] issues, GHRepository owner) {
        for (GHIssue i : issues)
            i.wrap(owner);
        return issues;
    }

    /**
     * Repository to which the issue belongs.
     *
     * @return the repository
     */
    public GHRepository getRepository() {
        return owner;
    }

    /**
     * The description of this pull request.
     *
     * @return the body
     */
    public String getBody() {
        return body;
    }

    /**
     * ID.
     *
     * @return the number
     */
    public int getNumber() {
        return number;
    }

    /**
     * The HTML page of this issue, like https://github.com/jenkinsci/jenkins/issues/100
     */
    public URL getHtmlUrl() {
        return GitHub.parseURL(html_url);
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
     * Is locked boolean.
     *
     * @return the boolean
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * Gets state.
     *
     * @return the state
     */
    public GHIssueState getState() {
        return Enum.valueOf(GHIssueState.class, state.toUpperCase(Locale.ENGLISH));
    }

    /**
     * Gets labels.
     *
     * @return the labels
     * @throws IOException
     *             the io exception
     */
    public Collection<GHLabel> getLabels() throws IOException {
        if (labels == null) {
            return Collections.emptyList();
        }
        return Collections.<GHLabel>unmodifiableList(labels);
    }

    /**
     * Gets closed at.
     *
     * @return the closed at
     */
    public Date getClosedAt() {
        return GitHub.parseDate(closed_at);
    }

    /**
     * Gets api url.
     *
     * @return the api url
     */
    public URL getApiURL() {
        return GitHub.parseURL(url);
    }

    /**
     * Lock.
     *
     * @throws IOException
     *             the io exception
     */
    public void lock() throws IOException {
        root.retrieve().method("PUT").withUrlPath(getApiRoute() + "/lock").to();
    }

    /**
     * Unlock.
     *
     * @throws IOException
     *             the io exception
     */
    public void unlock() throws IOException {
        root.retrieve().method("PUT").withUrlPath(getApiRoute() + "/lock").to();
    }

    /**
     * Updates the issue by adding a comment.
     *
     * @param message
     *            the message
     * @return Newly posted comment.
     * @throws IOException
     *             the io exception
     */
    @WithBridgeMethods(void.class)
    public GHIssueComment comment(String message) throws IOException {
        GHIssueComment r = root.retrieve()
                .method("POST")
                .with("body", message)
                .withUrlPath(getIssuesApiRoute() + "/comments")
                .to(GHIssueComment.class);
        return r.wrapUp(this);
    }

    private void edit(String key, Object value) throws IOException {
        root.retrieve().with(key, value).method("PATCH").withUrlPath(getApiRoute()).to();
    }

    private void editIssue(String key, Object value) throws IOException {
        root.retrieve().with(key, value).method("PATCH").withUrlPath(getIssuesApiRoute()).to();
    }

    /**
     * Closes this issue.
     *
     * @throws IOException
     *             the io exception
     */
    public void close() throws IOException {
        edit("state", "closed");
    }

    /**
     * Reopens this issue.
     *
     * @throws IOException
     *             the io exception
     */
    public void reopen() throws IOException {
        edit("state", "open");
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
     * Sets body.
     *
     * @param body
     *            the body
     * @throws IOException
     *             the io exception
     */
    public void setBody(String body) throws IOException {
        edit("body", body);
    }

    /**
     * Sets milestone.
     *
     * @param milestone
     *            the milestone
     * @throws IOException
     *             the io exception
     */
    public void setMilestone(GHMilestone milestone) throws IOException {
        edit("milestone", milestone.getNumber());
    }

    /**
     * Assign to.
     *
     * @param user
     *            the user
     * @throws IOException
     *             the io exception
     */
    public void assignTo(GHUser user) throws IOException {
        setAssignees(user);
    }

    /**
     * Sets labels.
     *
     * @param labels
     *            the labels
     * @throws IOException
     *             the io exception
     */
    public void setLabels(String... labels) throws IOException {
        editIssue("labels", labels);
    }

    /**
     * Adds labels to the issue.
     *
     * @param names
     *            Names of the label
     * @throws IOException
     *             the io exception
     */
    public void addLabels(String... names) throws IOException {
        _addLabels(Arrays.asList(names));
    }

    /**
     * Add labels.
     *
     * @param labels
     *            the labels
     * @throws IOException
     *             the io exception
     */
    public void addLabels(GHLabel... labels) throws IOException {
        addLabels(Arrays.asList(labels));
    }

    /**
     * Add labels.
     *
     * @param labels
     *            the labels
     * @throws IOException
     *             the io exception
     */
    public void addLabels(Collection<GHLabel> labels) throws IOException {
        _addLabels(GHLabel.toNames(labels));
    }

    private void _addLabels(Collection<String> names) throws IOException {
        List<String> newLabels = new ArrayList<String>();

        for (GHLabel label : getLabels()) {
            newLabels.add(label.getName());
        }
        for (String name : names) {
            if (!newLabels.contains(name)) {
                newLabels.add(name);
            }
        }
        setLabels(newLabels.toArray(new String[0]));
    }

    /**
     * Remove a given label by name from this issue.
     *
     * @param names
     *            the names
     * @throws IOException
     *             the io exception
     */
    public void removeLabels(String... names) throws IOException {
        _removeLabels(Arrays.asList(names));
    }

    /**
     * Remove labels.
     *
     * @param labels
     *            the labels
     * @throws IOException
     *             the io exception
     * @see #removeLabels(String...) #removeLabels(String...)
     */
    public void removeLabels(GHLabel... labels) throws IOException {
        removeLabels(Arrays.asList(labels));
    }

    /**
     * Remove labels.
     *
     * @param labels
     *            the labels
     * @throws IOException
     *             the io exception
     */
    public void removeLabels(Collection<GHLabel> labels) throws IOException {
        _removeLabels(GHLabel.toNames(labels));
    }

    private void _removeLabels(Collection<String> names) throws IOException {
        List<String> newLabels = new ArrayList<String>();

        for (GHLabel l : getLabels()) {
            if (!names.contains(l.getName())) {
                newLabels.add(l.getName());
            }
        }

        setLabels(newLabels.toArray(new String[0]));
    }

    /**
     * Obtains all the comments associated with this issue.
     *
     * @return the comments
     * @throws IOException
     *             the io exception
     * @see #listComments() #listComments()
     */
    public List<GHIssueComment> getComments() throws IOException {
        return listComments().asList();
    }

    /**
     * Obtains all the comments associated with this issue.
     *
     * @return the paged iterable
     * @throws IOException
     *             the io exception
     */
    public PagedIterable<GHIssueComment> listComments() throws IOException {
        return root.retrieve()
                .asPagedIterable(getIssuesApiRoute() + "/comments",
                        GHIssueComment[].class,
                        item -> item.wrapUp(GHIssue.this));
    }

    @Preview
    @Deprecated
    public GHReaction createReaction(ReactionContent content) throws IOException {
        return owner.root.retrieve()
                .method("POST")
                .withPreview(SQUIRREL_GIRL)
                .with("content", content.getContent())
                .withUrlPath(getApiRoute() + "/reactions")
                .to(GHReaction.class)
                .wrap(root);
    }

    @Preview
    @Deprecated
    public PagedIterable<GHReaction> listReactions() {
        return owner.root.retrieve()
                .withPreview(SQUIRREL_GIRL)
                .asPagedIterable(getApiRoute() + "/reactions", GHReaction[].class, item -> item.wrap(owner.root));
    }

    /**
     * Add assignees.
     *
     * @param assignees
     *            the assignees
     * @throws IOException
     *             the io exception
     */
    public void addAssignees(GHUser... assignees) throws IOException {
        addAssignees(Arrays.asList(assignees));
    }

    /**
     * Add assignees.
     *
     * @param assignees
     *            the assignees
     * @throws IOException
     *             the io exception
     */
    public void addAssignees(Collection<GHUser> assignees) throws IOException {
        root.retrieve()
                .method("POST")
                .with(ASSIGNEES, getLogins(assignees))
                .withUrlPath(getIssuesApiRoute() + "/assignees")
                .to(this);
    }

    /**
     * Sets assignees.
     *
     * @param assignees
     *            the assignees
     * @throws IOException
     *             the io exception
     */
    public void setAssignees(GHUser... assignees) throws IOException {
        setAssignees(Arrays.asList(assignees));
    }

    /**
     * Sets assignees.
     *
     * @param assignees
     *            the assignees
     * @throws IOException
     *             the io exception
     */
    public void setAssignees(Collection<GHUser> assignees) throws IOException {
        root.retrieve().method("PATCH").with(ASSIGNEES, getLogins(assignees)).withUrlPath(getIssuesApiRoute()).to();
    }

    /**
     * Remove assignees.
     *
     * @param assignees
     *            the assignees
     * @throws IOException
     *             the io exception
     */
    public void removeAssignees(GHUser... assignees) throws IOException {
        removeAssignees(Arrays.asList(assignees));
    }

    /**
     * Remove assignees.
     *
     * @param assignees
     *            the assignees
     * @throws IOException
     *             the io exception
     */
    public void removeAssignees(Collection<GHUser> assignees) throws IOException {
        root.retrieve()
                .method("DELETE")
                .with(ASSIGNEES, getLogins(assignees))
                .inBody()
                .withUrlPath(getIssuesApiRoute() + "/assignees")
                .to(this);
    }

    /**
     * Gets api route.
     *
     * @return the api route
     */
    protected String getApiRoute() {
        return getIssuesApiRoute();
    }

    /**
     * Gets issues api route.
     *
     * @return the issues api route
     */
    protected String getIssuesApiRoute() {
        return "/repos/" + owner.getOwnerName() + "/" + owner.getName() + "/issues/" + number;
    }

    /**
     * Gets assignee.
     *
     * @return the assignee
     * @throws IOException
     *             the io exception
     */
    public GHUser getAssignee() throws IOException {
        return root.intern(assignee);
    }

    /**
     * Gets assignees.
     *
     * @return the assignees
     */
    public List<GHUser> getAssignees() {
        return Collections.unmodifiableList(Arrays.asList(assignees));
    }

    /**
     * User who submitted the issue.
     *
     * @return the user
     * @throws IOException
     *             the io exception
     */
    public GHUser getUser() throws IOException {
        return root.intern(user);
    }

    /**
     * Reports who has closed the issue.
     *
     * <p>
     * Note that GitHub doesn't always seem to report this information even for an issue that's already closed. See
     * https://github.com/kohsuke/github-api/issues/60.
     *
     * @return the closed by
     * @throws IOException
     *             the io exception
     */
    public GHUser getClosedBy() throws IOException {
        if (!"closed".equals(state))
            return null;

        // TODO
        /*
         * if (closed_by==null) { closed_by = owner.getIssue(number).getClosed_by(); }
         */
        return root.intern(closed_by);
    }

    /**
     * Gets comments count.
     *
     * @return the comments count
     */
    public int getCommentsCount() {
        return comments;
    }

    /**
     * Returns non-null if this issue is a shadow of a pull request.
     *
     * @return the pull request
     */
    public PullRequest getPullRequest() {
        return pull_request;
    }

    /**
     * Is pull request boolean.
     *
     * @return the boolean
     */
    public boolean isPullRequest() {
        return pull_request != null;
    }

    /**
     * Gets milestone.
     *
     * @return the milestone
     */
    public GHMilestone getMilestone() {
        return milestone;
    }

    /**
     * The type PullRequest.
     */
    @SuppressFBWarnings(value = { "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD" },
            justification = "JSON API")
    public static class PullRequest {
        private String diff_url, patch_url, html_url;

        /**
         * Gets diff url.
         *
         * @return the diff url
         */
        public URL getDiffUrl() {
            return GitHub.parseURL(diff_url);
        }

        /**
         * Gets patch url.
         *
         * @return the patch url
         */
        public URL getPatchUrl() {
            return GitHub.parseURL(patch_url);
        }

        /**
         * Gets url.
         *
         * @return the url
         */
        public URL getUrl() {
            return GitHub.parseURL(html_url);
        }
    }

    protected static List<String> getLogins(Collection<GHUser> users) {
        List<String> names = new ArrayList<String>(users.size());
        for (GHUser a : users) {
            names.add(a.getLogin());
        }
        return names;
    }

    /**
     * Lists events for this issue. See https://developer.github.com/v3/issues/events/
     *
     * @return the paged iterable
     * @throws IOException
     *             the io exception
     */
    public PagedIterable<GHIssueEvent> listEvents() throws IOException {
        return root.retrieve()
                .asPagedIterable(owner.getApiTailUrl(String.format("/issues/%s/events", number)),
                        GHIssueEvent[].class,
                        item -> item.wrapUp(GHIssue.this));
    }
}
