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
public class GHIssue extends GHObject {
    GitHub root;
    GHRepository owner;
    
    // API v3
    protected GHUser assignee;
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

    /**
     * Updates the issue by adding a comment.
     *
     * @return
     *      Newly posted comment.
     */
    @WithBridgeMethods(void.class)
    public GHIssueComment comment(String message) throws IOException {
        GHIssueComment r = new Requester(root).with("body",message).to(getIssuesApiRoute() + "/comments", GHIssueComment.class);
        return r.wrapUp(this);
    }

    private void edit(String key, Object value) throws IOException {
        new Requester(root)._with(key, value).method("PATCH").to(getApiRoute());
    }

    private void editIssue(String key, Object value) throws IOException {
        new Requester(root)._with(key, value).method("PATCH").to(getIssuesApiRoute());
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

    public void assignTo(GHUser user) throws IOException {
        editIssue("assignee", user.getLogin());
    }

    public void setLabels(String... labels) throws IOException {
        editIssue("labels",labels);
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
        return new PagedIterable<GHIssueComment>() {
            public PagedIterator<GHIssueComment> _iterator(int pageSize) {
                return new PagedIterator<GHIssueComment>(root.retrieve().asIterator(getIssuesApiRoute() + "/comments", GHIssueComment[].class, pageSize)) {
                    protected void wrapUp(GHIssueComment[] page) {
                        for (GHIssueComment c : page)
                            c.wrapUp(GHIssue.this);
                    }
                };
            }
        };
    }

    protected String getApiRoute() {
        return getIssuesApiRoute();
    }

    protected String getIssuesApiRoute() {
        return "/repos/"+owner.getOwnerName()+"/"+owner.getName()+"/issues/"+number;
    }

    public GHUser getAssignee() {
        return assignee;
    }
    
    /**
     * User who submitted the issue.
     */
    public GHUser getUser() {
        return user;
    }

    /**
     * Reports who has closed the issue.
     *
     * <p>
     * Note that GitHub doesn't always seem to report this information
     * even for an issue that's already closed. See
     * https://github.com/kohsuke/github-api/issues/60.
     */
    public GHUser getClosedBy() {
        if(!"closed".equals(state)) return null;
        if(closed_by != null) return closed_by;
        
        //TODO closed_by = owner.getIssue(number).getClosed_by();
        return closed_by;
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
}
