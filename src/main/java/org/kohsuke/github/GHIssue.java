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

import static org.kohsuke.github.Previews.SQUIRREL_GIRL;

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

/**
 * Represents an issue on GitHub.
 *
 * @author Eric Maupin
 * @author Kohsuke Kawaguchi
 * @see GHRepository#getIssue(int)
 * @see GitHub#searchIssues()
 * @see GHIssueSearchBuilder
 */
public class GHIssue extends GHObject implements Reactable{
    private static final String ASSIGNEES = "assignees";

    GitHub root;
    GHRepository owner;
    
    // API v3
    protected GHUser assignee;  // not sure what this field is now that 'assignees' exist
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
     * @deprecated use {@link GHLabel}
     */
    public static class Label extends GHLabel {
    }
    
    /*package*/ GHIssue wrap(GHRepository owner) {
        this.owner = owner;
        if(milestone != null) milestone.wrap(owner);
        return wrap(owner.root);
    }

    /*package*/ GHIssue wrap(GitHub root) {
        this.root = root;
        if(assignee != null) assignee.wrapUp(root);
        if(assignees!=null)    GHUser.wrap(assignees,root);
        if(user != null) user.wrapUp(root);
        if(closed_by != null) closed_by.wrapUp(root);
        return this;
    }

    /*package*/ static GHIssue[] wrap(GHIssue[] issues, GHRepository owner) {
        for (GHIssue i : issues)
            i.wrap(owner);
        return issues;
    }

    /**
     * Repository to which the issue belongs.
     */
    public GHRepository getRepository() {
        return owner;
    }

    /**
     * The description of this pull request.
     */
    public String getBody() {
        return body;
    }

    /**
     * ID.
     */
    public int getNumber() {
        return number;
    }

    /**
     * The HTML page of this issue,
     * like https://github.com/jenkinsci/jenkins/issues/100
     */
    public URL getHtmlUrl() {
        return GitHub.parseURL(html_url);
    }

    public String getTitle() {
        return title;
    }

    public boolean isLocked() {
        return locked;
    }

    public GHIssueState getState() {
        return Enum.valueOf(GHIssueState.class, state.toUpperCase(Locale.ENGLISH));
    }

    public Collection<GHLabel> getLabels() throws IOException {
        if(labels == null){
            return Collections.emptyList();
        }
        return Collections.<GHLabel>unmodifiableList(labels);
    }

    public Date getClosedAt() {
        return GitHub.parseDate(closed_at);
    }

    public URL getApiURL(){
        return GitHub.parseURL(url);
    }

    public void lock() throws IOException {
        root.createRequester().method("PUT").to(getApiRoute()+"/lock");
    }

    public void unlock() throws IOException {
        root.createRequester().method("PUT").to(getApiRoute()+"/lock");
    }

    /**
     * Updates the issue by adding a comment.
     *
     * @return
     *      Newly posted comment.
     */
    @WithBridgeMethods(void.class)
    public GHIssueComment comment(String message) throws IOException {
        GHIssueComment r = root.createRequester().with("body",message).to(getIssuesApiRoute() + "/comments", GHIssueComment.class);
        return r.wrapUp(this);
    }

    private void edit(String key, Object value) throws IOException {
        root.createRequester().with(key, value).method("PATCH").to(getApiRoute());
    }

    private void editIssue(String key, Object value) throws IOException {
        root.createRequester().with(key, value).method("PATCH").to(getIssuesApiRoute());
    }

    /**
     * Closes this issue.
     */
    public void close() throws IOException {
        edit("state", "closed");
    }

    /**
     * Reopens this issue.
     */
    public void reopen() throws IOException {
        edit("state", "open");
    }

    public void setTitle(String title) throws IOException {
        edit("title",title);
    }

    public void setBody(String body) throws IOException {
        edit("body",body);
    }

    public void setMilestone(GHMilestone milestone) throws IOException {
        edit("milestone",milestone.getNumber());
    }

    public void assignTo(GHUser user) throws IOException {
        setAssignees(user);
    }

    public void setLabels(String... labels) throws IOException {
        editIssue("labels",labels);
    }

    /**
     * Adds labels to the issue.
     *
     * @param names Names of the label
     */
    public void addLabels(String... names) throws IOException {
        _addLabels(Arrays.asList(names));
    }

    public void addLabels(GHLabel... labels) throws IOException {
        addLabels(Arrays.asList(labels));
    }

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
     */
    public void removeLabels(String... names) throws IOException {
        _removeLabels(Arrays.asList(names));
    }

    /**
     * @see #removeLabels(String...)
     */
    public void removeLabels(GHLabel... labels) throws IOException {
        removeLabels(Arrays.asList(labels));
    }

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
     * @see #listComments() 
     */
    public List<GHIssueComment> getComments() throws IOException {
        return listComments().asList();
    }
    
    /**
     * Obtains all the comments associated with this issue.
     */
    public PagedIterable<GHIssueComment> listComments() throws IOException {
        return root.createRequester().method("GET")
            .asPagedIterable(
                getIssuesApiRoute() + "/comments",
                GHIssueComment[].class,
                item -> item.wrapUp(GHIssue.this) );
    }

    @Preview @Deprecated
    public GHReaction createReaction(ReactionContent content) throws IOException {
        return owner.root.createRequester()
            .withPreview(SQUIRREL_GIRL)
                .with("content", content.getContent())
                .to(getApiRoute()+"/reactions", GHReaction.class).wrap(root);
    }

    @Preview @Deprecated
    public PagedIterable<GHReaction> listReactions() {
        return owner.root.createRequester().method("GET").withPreview(SQUIRREL_GIRL)
            .asPagedIterable(
                getApiRoute()+"/reactions",
                GHReaction[].class,
                item -> item.wrap(owner.root) );
    }

    public void addAssignees(GHUser... assignees) throws IOException {
        addAssignees(Arrays.asList(assignees));
    }

    public void addAssignees(Collection<GHUser> assignees) throws IOException {
        List<String> logins = getLogins(assignees);
        root.createRequester().method("POST").with(ASSIGNEES,logins).to(getIssuesApiRoute()+"/assignees",this);
    }

    public void setAssignees(GHUser... assignees) throws IOException {
        setAssignees(Arrays.asList(assignees));
    }

    public void setAssignees(Collection<GHUser> assignees) throws IOException {
        List<String> logins = getLogins(assignees);
        root.createRequester().with(ASSIGNEES, logins).method("PATCH").to(getIssuesApiRoute());
    }

    public void removeAssignees(GHUser... assignees) throws IOException {
        removeAssignees(Arrays.asList(assignees));
    }

    public void removeAssignees(Collection<GHUser> assignees) throws IOException {
        List<String> logins = getLogins(assignees);
        root.createRequester().method("DELETE").with(ASSIGNEES,logins).inBody().to(getIssuesApiRoute()+"/assignees",this);
    }

    protected static List<String> getLogins(Collection<GHUser> users) {
        List<String> names = new ArrayList<String>(users.size());
        for (GHUser a : users) {
            names.add(a.getLogin());
        }
        return names;
    }

    protected String getApiRoute() {
        return getIssuesApiRoute();
    }

    protected String getIssuesApiRoute() {
        return "/repos/"+owner.getOwnerName()+"/"+owner.getName()+"/issues/"+number;
    }

    public GHUser getAssignee() throws IOException {
        return root.intern(assignee);
    }

    public List<GHUser> getAssignees() {
        return Collections.unmodifiableList(Arrays.asList(assignees));
    }

    /**
     * User who submitted the issue.
     */
    public GHUser getUser() throws IOException {
        return root.intern(user);
    }

    /**
     * Reports who has closed the issue.
     *
     * <p>
     * Note that GitHub doesn't always seem to report this information
     * even for an issue that's already closed. See
     * https://github.com/kohsuke/github-api/issues/60.
     */
    public GHUser getClosedBy() throws IOException {
        if(!"closed".equals(state)) return null;

        //TODO
        /*
        if (closed_by==null) {
            closed_by = owner.getIssue(number).getClosed_by();
        }
        */
        return root.intern(closed_by);
    }
    
    public int getCommentsCount(){
        return comments;
    }

    /**
     * Returns non-null if this issue is a shadow of a pull request.
     */
    public PullRequest getPullRequest() {
        return pull_request;
    }

    public boolean isPullRequest() {
        return pull_request!=null;
    }

    public GHMilestone getMilestone() {
        return milestone;
    }

    @SuppressFBWarnings(value = {"UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_FIELD"}, 
        justification = "JSON API")
    public static class PullRequest{
        private String diff_url, patch_url, html_url;
        
        public URL getDiffUrl() {
            return GitHub.parseURL(diff_url);
        }
        
        public URL getPatchUrl() {
            return GitHub.parseURL(patch_url);
        }
        
        public URL getUrl() {
            return GitHub.parseURL(html_url);
        }
    }

    /**
     * Lists events for this issue.
     * See https://developer.github.com/v3/issues/events/
     */
    public PagedIterable<GHIssueEvent> listEvents() throws IOException {
        return root.createRequester().method("GET").asPagedIterable(
            owner.getApiTailUrl(String.format("/issues/%s/events", number)),
            GHIssueEvent[].class,
            item -> item.wrapUp(GHIssue.this) );
    }
}
