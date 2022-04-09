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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static org.kohsuke.github.internal.Previews.SQUIRREL_GIRL;

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
    protected List<GHLabel> labels;
    protected GHUser user;
    protected String title, html_url;
    protected GHIssue.PullRequest pull_request;
    protected GHMilestone milestone;
    protected GHUser closed_by;
    protected boolean locked;

    GHIssue wrap(GHRepository owner) {
        this.owner = owner;
        if (milestone != null)
            milestone.lateBind(owner);
        return this;
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
        return GitHubClient.parseURL(html_url);
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
     */
    public Collection<GHLabel> getLabels() {
        if (labels == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(labels);
    }

    /**
     * Gets closed at.
     *
     * @return the closed at
     */
    public Date getClosedAt() {
        return GitHubClient.parseDate(closed_at);
    }

    /**
     * Gets api url.
     *
     * @return API URL of this object.
     * @deprecated use {@link #getUrl()}
     */
    @Deprecated
    public URL getApiURL() {
        return getUrl();
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
     * Unlock.
     *
     * @throws IOException
     *             the io exception
     */
    public void unlock() throws IOException {
        root().createRequest().method("DELETE").withUrlPath(getApiRoute() + "/lock").send();
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
        GHIssueComment r = root().createRequest()
                .method("POST")
                .with("body", message)
                .withUrlPath(getIssuesApiRoute() + "/comments")
                .fetch(GHIssueComment.class);
        return r.wrapUp(this);
    }

    private void edit(String key, Object value) throws IOException {
        root().createRequest().with(key, value).method("PATCH").withUrlPath(getApiRoute()).send();
    }

    /**
     * Identical to edit(), but allows null for the value.
     */
    private void editNullable(String key, Object value) throws IOException {
        root().createRequest().withNullable(key, value).method("PATCH").withUrlPath(getApiRoute()).send();
    }

    private void editIssue(String key, Object value) throws IOException {
        root().createRequest().withNullable(key, value).method("PATCH").withUrlPath(getIssuesApiRoute()).send();
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
     * Adds labels to the issue.
     *
     * Labels that are already present on the target are ignored.
     *
     * @return the complete list of labels including the new additions
     * @param names
     *            Names of the label
     * @throws IOException
     *             the io exception
     */
    @WithBridgeMethods(void.class)
    public List<GHLabel> addLabels(String... names) throws IOException {
        return _addLabels(Arrays.asList(names));
    }

    /**
     * Add labels.
     *
     * Labels that are already present on the target are ignored.
     *
     * @return the complete list of labels including the new additions
     * @param labels
     *            the labels
     * @throws IOException
     *             the io exception
     */
    @WithBridgeMethods(void.class)
    public List<GHLabel> addLabels(GHLabel... labels) throws IOException {
        return addLabels(Arrays.asList(labels));
    }

    /**
     * Add labels.
     *
     * Labels that are already present on the target are ignored.
     *
     * @return the complete list of labels including the new additions
     * @param labels
     *            the labels
     * @throws IOException
     *             the io exception
     */
    @WithBridgeMethods(void.class)
    public List<GHLabel> addLabels(Collection<GHLabel> labels) throws IOException {
        return _addLabels(GHLabel.toNames(labels));
    }

    private List<GHLabel> _addLabels(Collection<String> names) throws IOException {
        return Arrays.asList(root().createRequest()
                .with("labels", names)
                .method("POST")
                .withUrlPath(getIssuesApiRoute() + "/labels")
                .fetch(GHLabel[].class));
    }

    /**
     * Remove a single label.
     *
     * Attempting to remove a label that is not present throws {@link GHFileNotFoundException}.
     *
     * @return the remaining list of labels
     * @param name
     *            the name
     * @throws IOException
     *             the io exception, throws {@link GHFileNotFoundException} if label was not present.
     */
    @WithBridgeMethods(void.class)
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
     * @return the remaining list of labels
     * @param names
     *            the names
     * @throws IOException
     *             the io exception
     */
    @WithBridgeMethods(void.class)
    public List<GHLabel> removeLabels(String... names) throws IOException {
        return _removeLabels(Arrays.asList(names));
    }

    /**
     * Remove a collection of labels.
     *
     * Attempting to remove labels that are not present on the target are ignored.
     *
     * @return the remaining list of labels
     * @param labels
     *            the labels
     * @throws IOException
     *             the io exception
     * @see #removeLabels(String...) #removeLabels(String...)
     */
    @WithBridgeMethods(void.class)
    public List<GHLabel> removeLabels(GHLabel... labels) throws IOException {
        return removeLabels(Arrays.asList(labels));
    }

    /**
     * Remove a collection of labels.
     *
     * Attempting to remove labels that are not present on the target are ignored.
     *
     * @return the remaining list of labels
     * @param labels
     *            the labels
     * @throws IOException
     *             the io exception
     */
    @WithBridgeMethods(void.class)
    public List<GHLabel> removeLabels(Collection<GHLabel> labels) throws IOException {
        return _removeLabels(GHLabel.toNames(labels));
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
     * Obtains all the comments associated with this issue.
     *
     * @return the paged iterable
     * @throws IOException
     *             the io exception
     */
    public PagedIterable<GHIssueComment> listComments() throws IOException {
        return root().createRequest()
                .withUrlPath(getIssuesApiRoute() + "/comments")
                .toIterable(GHIssueComment[].class, item -> item.wrapUp(this));
    }

    @Preview(SQUIRREL_GIRL)
    public GHReaction createReaction(ReactionContent content) throws IOException {
        return root().createRequest()
                .method("POST")
                .withPreview(SQUIRREL_GIRL)
                .with("content", content.getContent())
                .withUrlPath(getApiRoute() + "/reactions")
                .fetch(GHReaction.class);
    }

    public void deleteReaction(GHReaction reaction) throws IOException {
        owner.root()
                .createRequest()
                .method("DELETE")
                .withUrlPath(getApiRoute(), "reactions", String.valueOf(reaction.getId()))
                .send();
    }

    @Preview(SQUIRREL_GIRL)
    public PagedIterable<GHReaction> listReactions() {
        return root().createRequest()
                .withPreview(SQUIRREL_GIRL)
                .withUrlPath(getApiRoute() + "/reactions")
                .toIterable(GHReaction[].class, null);
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
        root().createRequest()
                .method("POST")
                .with(ASSIGNEES, getLogins(assignees))
                .withUrlPath(getIssuesApiRoute() + "/assignees")
                .fetchInto(this);
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
        root().createRequest()
                .method("PATCH")
                .with(ASSIGNEES, getLogins(assignees))
                .withUrlPath(getIssuesApiRoute())
                .send();
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
        root().createRequest()
                .method("DELETE")
                .with(ASSIGNEES, getLogins(assignees))
                .inBody()
                .withUrlPath(getIssuesApiRoute() + "/assignees")
                .fetchInto(this);
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
     * Gets assignee.
     *
     * @return the assignee
     * @throws IOException
     *             the io exception
     */
    public GHUser getAssignee() throws IOException {
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
     * User who submitted the issue.
     *
     * @return the user
     * @throws IOException
     *             the io exception
     */
    public GHUser getUser() throws IOException {
        return root().intern(user);
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
        return root().intern(closed_by);
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
    @SuppressFBWarnings(value = { "EI_EXPOSE_REP" }, justification = "Expected behavior")
    public GHMilestone getMilestone() {
        return milestone;
    }

    /**
     * The type PullRequest.
     */
    @SuppressFBWarnings(value = { "UWF_UNWRITTEN_FIELD" }, justification = "JSON API")
    public static class PullRequest {
        private String diff_url, patch_url, html_url;

        /**
         * Gets diff url.
         *
         * @return the diff url
         */
        public URL getDiffUrl() {
            return GitHubClient.parseURL(diff_url);
        }

        /**
         * Gets patch url.
         *
         * @return the patch url
         */
        public URL getPatchUrl() {
            return GitHubClient.parseURL(patch_url);
        }

        /**
         * Gets url.
         *
         * @return the url
         */
        public URL getUrl() {
            return GitHubClient.parseURL(html_url);
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
        return root().createRequest()
                .withUrlPath(getRepository().getApiTailUrl(String.format("/issues/%s/events", number)))
                .toIterable(GHIssueEvent[].class, item -> item.wrapUp(this));
    }
}
