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
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.internal.EnumUtils;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

// TODO: Auto-generated Javadoc
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

    /**
     * The type PullRequest.
     */
    @SuppressFBWarnings(value = { "UWF_UNWRITTEN_FIELD" }, justification = "JSON API")
    public static class PullRequest {

        private String diffUrl, patchUrl, htmlUrl;

        /**
         * Create default PullRequest instance
         */
        public PullRequest() {
        }

        /**
         * Gets diff url.
         *
         * @return the diff url
         */
        public URL getDiffUrl() {
            return GitHubClient.parseURL(diffUrl);
        }

        /**
         * Gets patch url.
         *
         * @return the patch url
         */
        public URL getPatchUrl() {
            return GitHubClient.parseURL(patchUrl);
        }

        /**
         * Gets url.
         *
         * @return the url
         */
        public URL getUrl() {
            return GitHubClient.parseURL(htmlUrl);
        }
    }

    private static final String ASSIGNEES = "assignees";

    /**
     * Gets the logins.
     *
     * @param users
     *            the users
     * @return the logins
     */
    protected static List<String> getLogins(Collection<GHUser> users) {
        List<String> names = new ArrayList<String>(users.size());
        for (GHUser a : users) {
            names.add(a.getLogin());
        }
        return names;
    }

    /** The assignee. */
    // API v3
    protected GHUser assignee; // not sure what this field is now that 'assignees' exist

    /** The assignees. */
    protected GHUser[] assignees;

    /** The body. */
    @SkipFromToString
    protected String body;

    /** The closed at. */
    protected String closedAt;

    /** The closed by. */
    protected GHUser closedBy;

    /** The comments. */
    protected int comments;

    /** The labels. */
    protected List<GHLabel> labels;

    /** The locked. */
    protected boolean locked;

    /** The milestone. */
    protected GHMilestone milestone;

    /** The number. */
    protected int number;

    /** The pull request. */
    protected GHIssue.PullRequest pullRequest;

    /** The state. */
    protected String state;

    /** The state reason. */
    protected String stateReason;

    /** The html url. */
    protected String title, htmlUrl;

    /** The user. */
    protected GHUser user;

    /** The owner. */
    GHRepository owner;

    /**
     * Create default GHIssue instance
     */
    public GHIssue() {
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
        root().createRequest()
                .method("POST")
                .with(ASSIGNEES, getLogins(assignees))
                .withUrlPath(getIssuesApiRoute() + "/assignees")
                .fetchInto(this);
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
     * Add labels.
     *
     * Labels that are already present on the target are ignored.
     *
     * @param labels
     *            the labels
     * @return the complete list of labels including the new additions
     * @throws IOException
     *             the io exception
     */
    public List<GHLabel> addLabels(Collection<GHLabel> labels) throws IOException {
        return _addLabels(GHLabel.toNames(labels));
    }

    /**
     * Add labels.
     *
     * Labels that are already present on the target are ignored.
     *
     * @param labels
     *            the labels
     * @return the complete list of labels including the new additions
     * @throws IOException
     *             the io exception
     */
    public List<GHLabel> addLabels(GHLabel... labels) throws IOException {
        return addLabels(Arrays.asList(labels));
    }

    /**
     * Adds labels to the issue.
     *
     * Labels that are already present on the target are ignored.
     *
     * @param names
     *            Names of the label
     * @return the complete list of labels including the new additions
     * @throws IOException
     *             the io exception
     */
    public List<GHLabel> addLabels(String... names) throws IOException {
        return _addLabels(Arrays.asList(names));
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
     * Closes this issue.
     *
     * @throws IOException
     *             the io exception
     */
    public void close() throws IOException {
        edit("state", "closed");
    }

    /**
     * Closes this issue.
     *
     * @param reason
     *            the reason the issue was closed
     * @throws IOException
     *             the io exception
     */
    public void close(GHIssueStateReason reason) throws IOException {
        Map<String, Object> map = new HashMap<>();
        map.put("state", "closed");
        map.put("state_reason", reason.name().toLowerCase(Locale.ENGLISH));
        edit(map);
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
    public GHIssueComment comment(String message) throws IOException {
        GHIssueComment r = root().createRequest()
                .method("POST")
                .with("body", message)
                .withUrlPath(getIssuesApiRoute() + "/comments")
                .fetch(GHIssueComment.class);
        return r.wrapUp(this);
    }

    /**
     * Creates the reaction.
     *
     * @param content
     *            the content
     * @return the GH reaction
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public GHReaction createReaction(ReactionContent content) throws IOException {
        return root().createRequest()
                .method("POST")
                .with("content", content.getContent())
                .withUrlPath(getIssuesApiRoute() + "/reactions")
                .fetch(GHReaction.class);
    }

    /**
     * Delete reaction.
     *
     * @param reaction
     *            the reaction
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void deleteReaction(GHReaction reaction) throws IOException {
        owner.root()
                .createRequest()
                .method("DELETE")
                .withUrlPath(getIssuesApiRoute(), "reactions", String.valueOf(reaction.getId()))
                .send();
    }

    /**
     * Gets assignee.
     *
     * @return the assignee
     */
    public GHUser getAssignee() {
        return root().intern(assignee);
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
     * The description of this pull request.
     *
     * @return the body
     */
    public String getBody() {
        return body;
    }

    /**
     * Gets closed at.
     *
     * @return the closed at
     */
    @WithBridgeMethods(value = Date.class, adapterMethod = "instantToDate")
    public Instant getClosedAt() {
        return GitHubClient.parseInstant(closedAt);
    }

    /**
     * Reports who has closed the issue.
     *
     * <p>
     * Note that GitHub doesn't always seem to report this information even for an issue that's already closed. See
     * https://github.com/kohsuke/github-api/issues/60.
     *
     * @return the closed by
     */
    public GHUser getClosedBy() {
        if (!"closed".equals(state))
            return null;

        // TODO
        /*
         * if (closed_by==null) { closed_by = owner.getIssue(number).getClosed_by(); }
         */
        return root().intern(closedBy);
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
        return listComments().toList();
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
     * The HTML page of this issue, like https://github.com/jenkinsci/jenkins/issues/100
     *
     * @return the html url
     */
    public URL getHtmlUrl() {
        return GitHubClient.parseURL(htmlUrl);
    }

    /**
     * Gets labels.
     *
     * @return the labels
     */
    public Collection<GHLabel> getLabels() {
        if (labels == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(labels);
    }

    /**
     * Gets milestone.
     *
     * @return the milestone
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHMilestone getMilestone() {
        return milestone;
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
     * Returns non-null if this issue is a shadow of a pull request.
     *
     * @return the pull request
     */
    public PullRequest getPullRequest() {
        return pullRequest;
    }

    /**
     * Repository to which the issue belongs.
     *
     * @return the repository
     */
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHRepository getRepository() {
        try {
            synchronized (this) {
                if (owner == null) {
                    String repositoryUrlPath = getRepositoryUrlPath();
                    wrap(root().createRequest().withUrlPath(repositoryUrlPath).fetch(GHRepository.class));
                }
            }
        } catch (IOException e) {
            throw new GHException("Failed to fetch repository", e);
        }
        return owner;
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
     * Gets state reason.
     *
     * @return the state reason
     */
    public GHIssueStateReason getStateReason() {
        return EnumUtils.getNullableEnumOrDefault(GHIssueStateReason.class, stateReason, GHIssueStateReason.UNKNOWN);
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
     * User who submitted the issue.
     *
     * @return the user
     */
    public GHUser getUser() {
        return root().intern(user);
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
     * Is pull request boolean.
     *
     * @return the boolean
     */
    public boolean isPullRequest() {
        return pullRequest != null;
    }

    /**
     * Obtains all the comments associated with this issue, without any filter.
     *
     * @return the paged iterable
     * @see <a href="https://docs.github.com/en/rest/issues/comments#list-issue-comments">List issue comments</a>
     * @see #queryComments() queryComments to apply filters.
     */
    public PagedIterable<GHIssueComment> listComments() {
        return root().createRequest()
                .withUrlPath(getIssuesApiRoute() + "/comments")
                .toIterable(GHIssueComment[].class, item -> item.wrapUp(this));
    }

    /**
     * Lists events for this issue. See https://developer.github.com/v3/issues/events/
     *
     * @return the paged iterable
     */
    public PagedIterable<GHIssueEvent> listEvents() {
        return root().createRequest()
                .withUrlPath(getRepository().getApiTailUrl(String.format("/issues/%s/events", number)))
                .toIterable(GHIssueEvent[].class, item -> item.wrapUp(this));
    }

    /**
     * List reactions.
     *
     * @return the paged iterable
     */
    public PagedIterable<GHReaction> listReactions() {
        return root().createRequest()
                .withUrlPath(getIssuesApiRoute() + "/reactions")
                .toIterable(GHReaction[].class, null);
    }

    /**
     * Lock.
     *
     * @throws IOException
     *             the io exception
     */
    public void lock() throws IOException {
        root().createRequest().method("PUT").withUrlPath(getApiRoute() + "/lock").send();
    }

    /**
     * Search comments on this issue by specifying filters through a builder pattern.
     *
     * @return the query builder
     * @see <a href="https://docs.github.com/en/rest/issues/comments#list-issue-comments">List issue comments</a>
     */
    public GHIssueCommentQueryBuilder queryComments() {
        return new GHIssueCommentQueryBuilder(this);
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
        root().createRequest()
                .method("DELETE")
                .with(ASSIGNEES, getLogins(assignees))
                .inBody()
                .withUrlPath(getIssuesApiRoute() + "/assignees")
                .fetchInto(this);
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
     * Remove a single label.
     *
     * Attempting to remove a label that is not present throws {@link GHFileNotFoundException}.
     *
     * @param name
     *            the name
     * @return the remaining list of labels
     * @throws IOException
     *             the io exception, throws {@link GHFileNotFoundException} if label was not present.
     */
    public List<GHLabel> removeLabel(String name) throws IOException {
        return Arrays.asList(root().createRequest()
                .method("DELETE")
                .withUrlPath(getIssuesApiRoute() + "/labels", name)
                .fetch(GHLabel[].class));
    }

    /**
     * Remove a collection of labels.
     *
     * Attempting to remove labels that are not present on the target are ignored.
     *
     * @param labels
     *            the labels
     * @return the remaining list of labels
     * @throws IOException
     *             the io exception
     */
    public List<GHLabel> removeLabels(Collection<GHLabel> labels) throws IOException {
        return _removeLabels(GHLabel.toNames(labels));
    }

    /**
     * Remove a collection of labels.
     *
     * Attempting to remove labels that are not present on the target are ignored.
     *
     * @param labels
     *            the labels
     * @return the remaining list of labels
     * @throws IOException
     *             the io exception
     * @see #removeLabels(String...) #removeLabels(String...)
     */
    public List<GHLabel> removeLabels(GHLabel... labels) throws IOException {
        return removeLabels(Arrays.asList(labels));
    }

    /**
     * Remove a collection of labels.
     *
     * Attempting to remove labels that are not present on the target are ignored.
     *
     * @param names
     *            the names
     * @return the remaining list of labels
     * @throws IOException
     *             the io exception
     */
    public List<GHLabel> removeLabels(String... names) throws IOException {
        return _removeLabels(Arrays.asList(names));
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
     * Sets assignees.
     *
     * @param assignees
     *            the assignees
     * @throws IOException
     *             the io exception
     */
    public void setAssignees(Collection<GHUser> assignees) throws IOException {
        root().createRequest()
                .method("PATCH")
                .with(ASSIGNEES, getLogins(assignees))
                .withUrlPath(getIssuesApiRoute())
                .send();
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
     * Sets labels on the target to a specific list.
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
     * Sets the milestone for this issue.
     *
     * @param milestone
     *            The milestone to assign this issue to. Use null to remove the milestone for this issue.
     * @throws IOException
     *             The io exception
     */
    public void setMilestone(GHMilestone milestone) throws IOException {
        if (milestone == null) {
            editIssue("milestone", null);
        } else {
            editIssue("milestone", milestone.getNumber());
        }
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
     * Unlock.
     *
     * @throws IOException
     *             the io exception
     */
    public void unlock() throws IOException {
        root().createRequest().method("DELETE").withUrlPath(getApiRoute() + "/lock").send();
    }

    private List<GHLabel> _addLabels(Collection<String> names) throws IOException {
        return Arrays.asList(root().createRequest()
                .with("labels", names)
                .method("POST")
                .withUrlPath(getIssuesApiRoute() + "/labels")
                .fetch(GHLabel[].class));
    }

    private List<GHLabel> _removeLabels(Collection<String> names) throws IOException {
        List<GHLabel> remainingLabels = Collections.emptyList();
        for (String name : names) {
            try {
                remainingLabels = removeLabel(name);
            } catch (GHFileNotFoundException e) {
                // when trying to remove multiple labels, we ignore already removed
            }
        }
        return remainingLabels;
    }

    private void edit(Map<String, Object> map) throws IOException {
        root().createRequest().with(map).method("PATCH").withUrlPath(getApiRoute()).send();
    }

    private void edit(String key, Object value) throws IOException {
        root().createRequest().with(key, value).method("PATCH").withUrlPath(getApiRoute()).send();
    }

    private void editIssue(String key, Object value) throws IOException {
        root().createRequest().withNullable(key, value).method("PATCH").withUrlPath(getIssuesApiRoute()).send();
    }

    /**
     * Identical to edit(), but allows null for the value.
     */
    private void editNullable(String key, Object value) throws IOException {
        root().createRequest().withNullable(key, value).method("PATCH").withUrlPath(getApiRoute()).send();
    }

    private String getRepositoryUrlPath() {
        String url = getUrl().toString();
        int index = url.indexOf("/issues");
        if (index == -1) {
            index = url.indexOf("/pulls");
        }
        return url.substring(0, index);
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
        if (owner == null) {
            // Issues returned from search to do not have an owner. Attempt to use url.
            final URL url = Objects.requireNonNull(getUrl(), "Missing instance URL!");
            return StringUtils.prependIfMissing(url.toString().replace(root().getApiUrl(), ""), "/");
        }
        GHRepository repo = getRepository();
        return "/repos/" + repo.getOwnerName() + "/" + repo.getName() + "/issues/" + number;
    }

    /**
     * Wrap.
     *
     * @param owner
     *            the owner
     * @return the GH issue
     */
    GHIssue wrap(GHRepository owner) {
        this.owner = owner;
        if (milestone != null)
            milestone.lateBind(owner);
        return this;
    }
}
